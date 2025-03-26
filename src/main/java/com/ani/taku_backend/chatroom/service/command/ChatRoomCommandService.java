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
import com.ani.taku_backend.jangter.model.entity.DuckuJangter;
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

        DuckuJangter product = findProduct(requestDto.articleId());
        
        // 도메인 객체의 검증 메서드 호출
        product.validateForChatRoom();
        product.validateDifferentUsers(requestDto.buyerId());

        Long sellerId = product.getUser().getUserId();

        validateNewChatRoom(requestDto);

        User buyer = findUser(requestDto.buyerId());
        User seller = findUser(sellerId);

        ChatRoom chatRoom = ChatRoom.createChatRoom(requestDto.articleId(), buyer, seller);

        ChatRoom savedRoom = chatRoomRepository.save(chatRoom);

        ChatRoomMetaInfo metaInfo = createAndSaveChatRoomMetaInfo(savedRoom.getId(), requestDto.buyerId(), sellerId);

        return chatRoomQueryService.createChatRoomResponseDTO(
                savedRoom,
                metaInfo,
                ChatRoomUsers.of(buyer, seller),
                ChatRoomMessages.empty(),
                UnreadMessageCounts.of(savedRoom.getId(), 0),
                ArticleImage.fromProductImageDTOs(duckuJangterRepository.findProductImagesById(List.of(requestDto.articleId())))
        );
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
     * 상품을 조회합니다.
     */
    private DuckuJangter findProduct(Long articleId) {
        return duckuJangterRepository.findById(articleId)
                .orElseThrow(() -> new DuckwhoException(ErrorCode.NOT_FOUND_POST));
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

    /**
     * 중복 채팅방 생성을 방지하기 위한 검증 로직
     */
    private void validateNewChatRoom(ChatRoomRequestDTO requestDto) {
        // 1. 상품에 대한 모든 채팅방 조회
        List<ChatRoom> chatRooms = chatRoomRepository.findByArticleId(requestDto.articleId());
        if (chatRooms.isEmpty()) {
            return;
        }

        // 2. 활성 채팅방에 구매자로 참여 중인지 확인
        if (ChatRoom.hasActiveBuyerInRooms(chatRooms, requestDto.buyerId())) {
            throw new DuckwhoException(ErrorCode.DUPLICATE_CHAT_ROOM);
        }

        // 3. 메타 정보에서도 역할 확인 (모든 상태의 채팅방 고려)
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