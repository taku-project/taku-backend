package com.ani.taku_backend.chatroom.service.command;

import com.ani.taku_backend.chatroom.domain.document.ChatRoomMetaInfo;
import com.ani.taku_backend.chatroom.dto.ChatRoomAggregateResult;
import com.ani.taku_backend.chatroom.dto.ChatRoomDomainContext;
import com.ani.taku_backend.chatroom.dto.request.ChatRoomRequestDTO;
import com.ani.taku_backend.chatroom.dto.response.ChatRoomResponseDTO;
import com.ani.taku_backend.chatroom.domain.entity.ChatRoom;
import com.ani.taku_backend.chatroom.domain.repository.ChatRoomMetaRepository;
import com.ani.taku_backend.chatroom.domain.repository.ChatRoomRepository;
import com.ani.taku_backend.chatroom.domain.repository.ChatRoomRepositoryImpl;
import com.ani.taku_backend.chatroom.mapper.ChatRoomMapper;
import com.ani.taku_backend.common.exception.DuckwhoException;
import com.ani.taku_backend.common.exception.ErrorCode;
import com.ani.taku_backend.jangter.dto.ProductAggregateDTO;
import com.ani.taku_backend.jangter.model.entity.DuckuJangter;
import com.ani.taku_backend.jangter.repository.DuckuJangterRepository;
import com.ani.taku_backend.user.model.entity.User;
import com.ani.taku_backend.user.repository.UserRepository;
import com.ani.taku_backend.chatroom.service.query.ChatRoomQueryService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;


/**
 * 채팅방 애플리케이션 서비스
 * Command 파트를 담당하는 서비스입니다.
 * 채팅방 생성, 수정, 삭제 등과 같은 명령(Command)과 관련된 비즈니스 로직을 담당합니다.
 */
@Slf4j
@Service
@Transactional
public class ChatRoomCommandService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMetaRepository chatRoomMetaRepository;
    private final DuckuJangterRepository duckuJangterRepository;
    private final UserRepository userRepository;
    private final ChatRoomQueryService chatRoomQueryService;
    private final ChatRoomMapper chatRoomMapper;
    
    public ChatRoomCommandService(
            ChatRoomRepository chatRoomRepository,
            ChatRoomMetaRepository chatRoomMetaRepository,
            DuckuJangterRepository duckuJangterRepository,
            UserRepository userRepository,
            ChatRoomQueryService chatRoomQueryService,
            ChatRoomMapper chatRoomMapper) {
        this.chatRoomRepository = chatRoomRepository;
        this.chatRoomMetaRepository = chatRoomMetaRepository;
        this.duckuJangterRepository = duckuJangterRepository;
        this.userRepository = userRepository;
        this.chatRoomQueryService = chatRoomQueryService;
        this.chatRoomMapper = chatRoomMapper;
    }


    /**
     * 새로운 채팅방을 생성합니다.
     * 최적화: 기존 채팅방 재활성화 시 DB 접근 최소화 (1-2회)
     */
    public ChatRoomResponseDTO createChatRoom(ChatRoomRequestDTO requestDto) {
        // 1. 필수 데이터 한 번에 조회
        ChatRoomDomainContext context = fetchDomainEntities(requestDto);
        
        // 2. 도메인 검증
        validateChatRoomCreation(context);
        
        // 3. 비활성화된 채팅방이 있는지 확인
        Optional<ChatRoomRepositoryImpl.ChatRoomWithMeta> inactiveChatRoomWithMeta = 
                chatRoomRepository.findInactiveChatRoomWithMeta(requestDto.articleId(), requestDto.buyerId());
        
        if (inactiveChatRoomWithMeta.isPresent()) {
            // 3.1 비활성화된 채팅방과 메타정보가 있으면 재활성화
            ChatRoom chatRoom = inactiveChatRoomWithMeta.get().getChatRoom();
            ChatRoomMetaInfo metaInfo = inactiveChatRoomWithMeta.get().getMetaInfo();
            
            // 메타 정보에서 사용자 재활성화 (메타정보 이미 조회된 상태)
            boolean updated = metaInfo.getParticipants().activateParticipant(requestDto.buyerId());
            
            if (updated) {
                // 변경된 메타정보 저장 (1회 DB 접근)
                chatRoomMetaRepository.save(metaInfo);
                
                log.debug("기존 채팅방 재활성화: chatRoomId={}, buyerId={}", chatRoom.getId(), requestDto.buyerId());
                
                // 현재 채팅방 상태로 응답 DTO 생성을 위한 데이터 준비
                // 판매자와 구매자 정보 가져오기
                User buyer = context.getBuyer();
                User seller = context.getSeller();
                
                // 상품 정보 사용
                String articleImage = context.getArticleImage();
                String articleTitle = context.getArticleTitle();
                BigDecimal articlePrice = context.getArticlePrice();
                
                // 집계 결과 생성
                ChatRoomAggregateResult result = new ChatRoomAggregateResult(
                        chatRoom, metaInfo, buyer, seller, articleImage, articleTitle, articlePrice);
                
                // DTO 변환 및 반환
                return chatRoomMapper.toChatRoomResponseDTO(result);
            }
        }
        
        // 4. 비활성화된 채팅방이 없거나 재활성화에 실패한 경우 새 채팅방 생성
        ChatRoomAggregateResult result = createAndSaveChatRoomAggregate(context);
        
        // 5. DTO 변환
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
     * 비즈니스 규칙 검증
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