package com.ani.taku_backend.chatroom.service.command;

import com.ani.taku_backend.chatroom.domain.constant.ChatRoomStatus;
import com.ani.taku_backend.chatroom.domain.constant.JangterChatRole;
import com.ani.taku_backend.chatroom.domain.document.ChatRoomMetaInfo;
import com.ani.taku_backend.chatroom.dto.request.ChatRoomRequestDTO;
import com.ani.taku_backend.chatroom.dto.response.ChatRoomResponseDTO;
import com.ani.taku_backend.chatroom.domain.entity.ChatRoom;
import com.ani.taku_backend.chatroom.domain.repository.ChatRoomMetaRepository;
import com.ani.taku_backend.chatroom.domain.repository.ChatRoomRepository;
import com.ani.taku_backend.chatroom.domain.vo.ArticleImage;
import com.ani.taku_backend.chatroom.domain.vo.ChatRoomMessages;
import com.ani.taku_backend.chatroom.domain.vo.ChatRoomUsers;
import com.ani.taku_backend.chatroom.domain.vo.UnreadMessageCounts;
import com.ani.taku_backend.chatroom.mapper.ChatRoomDtoConverter;
import com.ani.taku_backend.common.exception.DuckwhoException;
import com.ani.taku_backend.common.exception.ErrorCode;
import com.ani.taku_backend.jangter.model.dto.ProductImageDTO;
import com.ani.taku_backend.jangter.model.entity.DuckuJangter;
import com.ani.taku_backend.jangter.model.enums.ProductStatus;
import com.ani.taku_backend.jangter.repository.DuckuJangterRepository;
import com.ani.taku_backend.user.model.entity.User;
import com.ani.taku_backend.user.repository.UserRepository;
import com.ani.taku_backend.chatroom.service.query.ChatRoomQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


/**
 * 채팅방 애플리케이션 서비스
 * Command 파트를 담당하는 서비스입니다.
 * 채팅방 생성, 수정, 삭제 등과 같은 명령(Command)과 관련된 비즈니스 로직을 담당합니다.
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ChatRoomCommandService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMetaRepository chatRoomMetaRepository;
    private final DuckuJangterRepository duckuJangterRepository;
    private final UserRepository userRepository;
    private final ChatRoomDtoConverter chatRoomDtoConverter;
    private final ChatRoomQueryService chatRoomQueryService;


    /**
     * 새로운 채팅방을 생성합니다.
     *
     * @param requestDto 채팅방 생성 요청 정보
     * @return 생성된 채팅방 정보
     */
    public ChatRoomResponseDTO createChatRoom(ChatRoomRequestDTO requestDto) {

        DuckuJangter product = findAndValidateProduct(requestDto.articleId());

        Long sellerId = product.getUser().getUserId();

        validateDifferentUsers(sellerId, requestDto.buyerId());

        validateNewChatRoom(requestDto);

        User buyer = findUser(requestDto.buyerId());
        User seller = findUser(sellerId);

        ChatRoom chatRoom = ChatRoom.createChatRoom(requestDto.articleId(), buyer, seller);

        ChatRoom savedRoom = chatRoomRepository.save(chatRoom);

        ChatRoomMetaInfo metaInfo = createAndSaveChatRoomMetaInfo(savedRoom.getId(), requestDto.buyerId(), sellerId);

        return createChatRoomResponseDTO(savedRoom, metaInfo, buyer, seller, requestDto.articleId());
    }

    /**
     * 참여자의 활성화 상태를 변경합니다.
     */
    public void updateParticipantActiveStatus(Long chatRoomId, Long userId, boolean active) {
        ChatRoomMetaInfo metaInfo = chatRoomMetaRepository.findByChatRoomId(chatRoomId)
                .orElseThrow(() -> new DuckwhoException(ErrorCode.CHAT_ROOM_NOT_FOUND));

        if (!metaInfo.getParticipants().containsUser(userId)) {
            throw new DuckwhoException(ErrorCode.INVALID_CHAT_USER);
        }
        
        if (active) {
            metaInfo.getParticipants().getInfo().get(userId).activate();
        } else {
            metaInfo.getParticipants().getInfo().get(userId).deactivate();
        }

        chatRoomMetaRepository.save(metaInfo);
    }

    /**
     * 상품을 조회하고 유효성을 검증합니다.
     */
    private DuckuJangter findAndValidateProduct(Long articleId) {
        DuckuJangter product = duckuJangterRepository.findById(articleId)
                .orElseThrow(() -> new DuckwhoException(ErrorCode.NOT_FOUND_POST));
        
        // 게시글 상태 확인 (판매중인 상태인지)
        if (product.getStatus() != ProductStatus.FOR_SALE) {
            throw new DuckwhoException(ErrorCode.INVALID_PRODUCT_STATUS);
        }
        
        return product;
    }

    /**
     * 구매자와 판매자가 다른 사용자인지 검증합니다.
     */
    private void validateDifferentUsers(Long sellerId, Long buyerId) {
        if (sellerId.equals(buyerId)) {
            throw new DuckwhoException(ErrorCode.INVALID_CHAT_USER);
        }
    }

    /**
     * 사용자를 조회합니다.
     */
    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new DuckwhoException(ErrorCode.USER_NOT_FOUND));
    }

    /**
     * 채팅방 메타 정보를 생성하고 저장합니다.
     */
    private ChatRoomMetaInfo createAndSaveChatRoomMetaInfo(Long chatRoomId, Long buyerId, Long sellerId) {
        ChatRoomMetaInfo metaInfo = ChatRoomMetaInfo.createWithParticipants(
                chatRoomId, buyerId, sellerId);
        
        return chatRoomMetaRepository.save(metaInfo);
    }


    private ChatRoomResponseDTO createChatRoomResponseDTO(
            ChatRoom savedRoom, ChatRoomMetaInfo metaInfo, User buyer, User seller, Long articleId) {

        List<ProductImageDTO> productImages = duckuJangterRepository.findProductImagesById(List.of(articleId));
        ArticleImage articleImage = ArticleImage.fromProductImageDTOs(productImages);

        ChatRoomMessages lastMessages = ChatRoomMessages.empty();

        // 신규 채팅방은 읽지 않은 메시지가 없음
        UnreadMessageCounts unreadCounts = UnreadMessageCounts.of(
                savedRoom.getId(),
                0
        );

        ChatRoomUsers users = ChatRoomUsers.of(buyer, seller);

        return chatRoomQueryService.createChatRoomResponseDTO(
                savedRoom,
                metaInfo,
                users,
                lastMessages,
                unreadCounts,
                articleImage
        );
    }


    /**
     * 중복 채팅방 생성을 방지하기 위한 검증 로직
     */
    private void validateNewChatRoom(ChatRoomRequestDTO requestDto) {
        List<ChatRoom> chatRooms = chatRoomRepository.findByArticleId(requestDto.articleId());
        if (chatRooms.isEmpty()) {
            return;
        }

        boolean hasExistingBuyer = chatRooms.stream()
                .filter(room -> room.getStatus() == ChatRoomStatus.ACTIVE)
                .flatMap(room -> room.getParticipants().stream())
                .anyMatch(participant ->
                        participant.isUser(requestDto.buyerId()) &&
                        participant.isBuyer());

        if (hasExistingBuyer) {
            throw new DuckwhoException(ErrorCode.DUPLICATE_CHAT_ROOM);
        }

        List<Long> chatRoomIds = chatRooms.stream()
                .map(ChatRoom::getId)
                .toList();

        List<ChatRoomMetaInfo> metaInfos = chatRoomMetaRepository.findByChatRoomIdIn(chatRoomIds);

        for (ChatRoomMetaInfo metaInfo : metaInfos) {
            if (metaInfo.getParticipants().hasUserWithRole(requestDto.buyerId(), JangterChatRole.BUYER)) {
                throw new DuckwhoException(ErrorCode.DUPLICATE_CHAT_ROOM);
            }
        }
    }
} 