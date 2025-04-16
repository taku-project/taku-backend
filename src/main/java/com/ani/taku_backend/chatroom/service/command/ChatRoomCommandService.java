package com.ani.taku_backend.chatroom.service.command;

import com.ani.taku_backend.chatroom.domain.document.ChatRoomMetaInfo;
import com.ani.taku_backend.chatroom.dto.ChatRoomAggregateResult;
import com.ani.taku_backend.chatroom.dto.ChatRoomDomainContext;
import com.ani.taku_backend.chatroom.dto.request.ChatRoomRequestDTO;
import com.ani.taku_backend.chatroom.dto.response.ChatRoomResponseDTO;
import com.ani.taku_backend.chatroom.domain.entity.ChatRoom;
import com.ani.taku_backend.chatroom.domain.repository.ChatRoomMetaRepository;
import com.ani.taku_backend.chatroom.domain.repository.ChatRoomRepository;
import com.ani.taku_backend.chatroom.mapper.ChatRoomDtoConverter;
import com.ani.taku_backend.chatroom.mapper.ChatRoomMapper;
import com.ani.taku_backend.common.exception.DuckwhoException;
import com.ani.taku_backend.common.exception.ErrorCode;
import com.ani.taku_backend.jangter.dto.ProductAggregateDTO;
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
    private final ChatRoomMapper chatRoomMapper;


    /**
     * 새로운 채팅방을 생성합니다.
     * DB 접근 3회로 최적화 (상품+유저 조회, 채팅방 저장, 메타정보 저장)
     */
    public ChatRoomResponseDTO createChatRoom(ChatRoomRequestDTO requestDto) {
        // 1. 필수 데이터 한 번에 조회
        ChatRoomDomainContext context = fetchDomainEntities(requestDto);
        
        // 2. 도메인 검증
        validateChatRoomCreation(context);

        // 3. 도메인 객체 생성 및 저장
        ChatRoomAggregateResult result = createAndSaveChatRoomAggregate(context);
        
        // 4. DTO 변환
        return chatRoomMapper.toChatRoomResponseDTO(result);
    }

    /**
     * 필요한 도메인 엔티티를 한 번에 조회
     */
    private ChatRoomDomainContext fetchDomainEntities(ChatRoomRequestDTO requestDto) {

        ProductAggregateDTO productAggregate = duckuJangterRepository
                .findProductAggregateById(requestDto.articleId())
                .orElseThrow(() -> new DuckwhoException(ErrorCode.NOT_FOUND_POST));

        Long buyerId = requestDto.buyerId();
        Long sellerId = productAggregate.getSellerId();
        
        List<User> users = userRepository.findByUserIdIn(List.of(buyerId, sellerId));
        User buyer = findUserById(users, buyerId);
        User seller = findUserById(users, sellerId);
        
        // 활성 채팅방 존재 여부 확인
        boolean hasActiveChatRoom = chatRoomRepository
                .existsActiveChatRoomByArticleIdAndBuyerId(
                        requestDto.articleId(), buyerId);
        
        return new ChatRoomDomainContext(
                productAggregate.getProduct(),
                buyer,
                seller,
                productAggregate.getArticleImage(),
                productAggregate.getArticleTitle(),
                productAggregate.getArticlePrice(),
                hasActiveChatRoom
        );
    }

    private User findUserById(List<User> users, Long userId) {
        return users.stream()
                .filter(user -> user.getUserId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new DuckwhoException(ErrorCode.USER_NOT_FOUND));
    }

    /**
     * 비즈니스 규칙 검증 (메모리 내 처리)
     */
    private void validateChatRoomCreation(ChatRoomDomainContext context) {
        DuckuJangter product = context.getProduct();
        
        // 도메인 검증 로직
        product.validateForChatRoom();
        
        // 구매자와 판매자가 동일한지 검증
        if (context.getBuyer().getUserId().equals(context.getSeller().getUserId())) {
            throw new DuckwhoException(ErrorCode.INVALID_INPUT_VALUE);
        }
        
        // 활성 채팅방 중복 검증
        if (context.isHasActiveChatRoom()) {
            throw new DuckwhoException(ErrorCode.DUPLICATE_CHAT_ROOM);
        }
    }

    /**
     * 도메인 객체 생성 및 저장
     */
    private ChatRoomAggregateResult createAndSaveChatRoomAggregate(ChatRoomDomainContext context) {
        // 1. 채팅방 생성 및 저장
        ChatRoom chatRoom = ChatRoom.createChatRoom(
                context.getProduct().getId(),
                context.getBuyer(),
                context.getSeller()
        );
        
        ChatRoom savedRoom = chatRoomRepository.save(chatRoom);
        
        // 2. 메타정보 생성 및 저장
        ChatRoomMetaInfo metaInfo = ChatRoomMetaInfo.createWithParticipants(
                savedRoom.getId(),
                context.getBuyer().getUserId(),
                context.getSeller().getUserId()
        );
        
        ChatRoomMetaInfo savedMetaInfo = chatRoomMetaRepository.save(metaInfo);
        
        return new ChatRoomAggregateResult(
                savedRoom,
                savedMetaInfo,
                context.getBuyer(),
                context.getSeller(),
                context.getArticleImage(),
                context.getArticleTitle(),
                context.getArticlePrice()
        );
    }

    /**
     * 참여자의 활성화 상태를 변경합니다.
     */
    public void updateParticipantActiveStatus(Long chatRoomId, Long userId, boolean active) {
        ChatRoomMetaInfo metaInfo = chatRoomMetaRepository.findByChatRoomId(chatRoomId)
                .orElseThrow(() -> new DuckwhoException(ErrorCode.CHAT_ROOM_NOT_FOUND));

        metaInfo.updateParticipantStatus(userId, active);

        chatRoomMetaRepository.save(metaInfo);
    }
} 