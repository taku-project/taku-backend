package com.ani.taku_backend.chatroom.service.query;

import com.ani.taku_backend.chatroom.contansts.MessageConstants;
import com.ani.taku_backend.chatroom.domain.constant.ChatRoomStatus;
import com.ani.taku_backend.chatroom.domain.constant.JangterChatRole;
import com.ani.taku_backend.chatroom.domain.document.ChatMessage;
import com.ani.taku_backend.chatroom.domain.document.ChatRoomMetaInfo;
import com.ani.taku_backend.chatroom.domain.vo.ArticleInfo;
import com.ani.taku_backend.chatroom.domain.vo.ChatRoomMetaInfoData;
import com.ani.taku_backend.chatroom.domain.vo.ChatRoomMetaInfos;
import com.ani.taku_backend.chatroom.dto.response.ChatMessageResponseDTO;
import com.ani.taku_backend.chatroom.dto.response.ChatRoomResponseDTO;
import com.ani.taku_backend.chatroom.domain.entity.ChatRoom;
import com.ani.taku_backend.chatroom.domain.repository.ChatRoomMetaRepository;
import com.ani.taku_backend.chatroom.domain.repository.ChatRoomRepository;
import com.ani.taku_backend.chatroom.domain.vo.ArticleImage;
import com.ani.taku_backend.chatroom.domain.vo.ChatRoomMessages;
import com.ani.taku_backend.chatroom.domain.vo.ChatRoomUsers;
import com.ani.taku_backend.chatroom.domain.vo.UnreadMessageCounts;
import com.ani.taku_backend.common.exception.DuckwhoException;
import com.ani.taku_backend.common.exception.ErrorCode;
import com.ani.taku_backend.jangter.model.dto.ArticleInfoDTO;
import com.ani.taku_backend.jangter.model.dto.ProductImageDTO;
import com.ani.taku_backend.jangter.model.entity.DuckuJangter;
import com.ani.taku_backend.jangter.repository.DuckuJangterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 채팅방 조회 서비스
 * Query 파트를 담당하는 서비스입니다.
 * 채팅방 조회와 관련된 비즈니스 로직만 담당합니다.
 */
@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ChatRoomQueryService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMetaRepository chatRoomMetaRepository;
    private final DuckuJangterRepository duckuJangterRepository;


    /**
     * 특정 채팅방의 정보를 조회합니다.
     *
     * @param roomId 채팅방 ID
     * @param userId 사용자 ID
     * @return 채팅방 정보
     */
    public ChatRoomResponseDTO findChatRoom(String roomId, Long userId) {
        // 1. 채팅방 조회 및 검증
        ChatRoom chatRoom = findChatRoomByWsRoomId(roomId);

        chatRoom.validateStatus();
        chatRoom.validateUserAccess(userId);

        // 2. 메타 정보 조회 및 사용자 접근 검증
        ChatRoomMetaInfo metaInfo = chatRoomMetaRepository.findByChatRoomId(chatRoom.getId())
                .orElseThrow(() -> new DuckwhoException(ErrorCode.CHAT_ROOM_NOT_FOUND));

        // 사용자가 채팅방을 나갔는지 확인 (비활성 상태 체크)
        validateMetaInfoAccess(chatRoom.getId(), userId);

        // 3. 필요한 데이터 준비
        ChatRoomUsers users = ChatRoomUsers.fromChatRoom(chatRoom);
        ChatRoomMessages lastMessages = ChatRoomMessages.of(chatRoom.getId(), metaInfo.getLastMessage());
        UnreadMessageCounts unreadCounts = UnreadMessageCounts.of(chatRoom.getId(), metaInfo.getUnreadCount(userId));

        // 상품 이미지와 상품 정보 조회
        Long articleId = chatRoom.getArticleId();
        ArticleImage articleImage = findArticleImage(List.of(articleId));
        ArticleInfo articleInfo = findArticleInfos(List.of(articleId));

        // 4. DTO 생성 및 반환
        return createChatRoomResponseDTO(
                chatRoom,
                metaInfo,
                users,
                lastMessages,
                unreadCounts,
                articleImage,
                articleInfo.getTitle(articleId),
                articleInfo.getPrice(articleId)
        );
    }


    /**
     * 사용자의 채팅방 목록을 페이징하여 조회합니다(무한 스크롤).
     *
     * @param userId 사용자 ID
     * @param pageable 페이징 정보
     * @return 채팅방 응답 DTO 목록의 Slice
     */
    public Slice<ChatRoomResponseDTO> findChatRoomListWithSlice(Long userId, Pageable pageable) {
        Slice<ChatRoom> chatRooms = chatRoomRepository.findChatRoomsWithSlice(
                userId, pageable, ChatRoomStatus.ACTIVE);

        if (!chatRooms.hasContent()) {
            return new SliceImpl<>(Collections.emptyList(), pageable, false);
        }

        List<Long> chatRoomIds = ChatRoom.extractChatRoomIds(chatRooms.getContent());
        List<Long> articleIds = ChatRoom.extractArticleIds(chatRooms.getContent());

        // 1. 상품 정보 효율적으로 조회
        ArticleInfo articleInfo = findArticleInfos(articleIds);

        // 2. 필요한 데이터 개별 준비
        ChatRoomMetaInfoData metaInfoData = getChatRoomMetaInfoData(chatRoomIds, userId);
        ChatRoomMetaInfos metaInfos = metaInfoData.getMetaInfos();
        ChatRoomMessages lastMessages = metaInfoData.getLastMessages();
        UnreadMessageCounts unreadCounts = metaInfoData.getUnreadCounts();
        ArticleImage articleImage = findArticleImage(articleIds);
        ChatRoomUsers users = ChatRoomUsers.fromChatRooms(chatRooms.getContent());

        // 3. DTO 변환
        List<ChatRoomResponseDTO> responseDTOs = chatRooms.getContent().stream()
                .map(room -> {
                    // 각 채팅방에 필요한 상품 정보 제공
                    Long articleId = room.getArticleId();
                    String title = articleInfo.getTitle(articleId);
                    BigDecimal price = articleInfo.getPrice(articleId);

                    return createChatRoomResponseDTO(
                            room,
                            metaInfos.getMetaInfo(room.getId()).orElse(null),
                            users,
                            lastMessages,
                            unreadCounts,
                            articleImage,
                            title,
                            price
                    );
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return new SliceImpl<>(responseDTOs, pageable, chatRooms.hasNext());
    }

    /**
     * 상품 이미지 URL 조회
     */
    private String getArticleImageUrl(Long articleId, ArticleImage articleImage) {
        try {
            return articleImage.getImageUrl(articleId);
        } catch (Exception e) {
            log.warn("상품 이미지 조회 중 오류 발생: articleId={}, error={}", articleId, e.getMessage());
            return null;
        }
    }

    /**
     * 읽지 않은 메시지 수 조회
     */
    private Integer getUnreadCount(Long chatRoomId, UnreadMessageCounts unreadCounts) {
        return unreadCounts.getUnreadCount(chatRoomId);
    }

    /**
     * 채팅방 응답 DTO를 생성합니다.
     * 각 계산값(lastMessageDTO, articleImageUrl, unreadCount)을 직접 계산하여 DTO 생성에 사용합니다.
     */
    public ChatRoomResponseDTO createChatRoomResponseDTO(
            ChatRoom chatRoom,
            ChatRoomMetaInfo metaInfo,
            ChatRoomUsers users,
            ChatRoomMessages lastMessages,
            UnreadMessageCounts unreadCounts,
            ArticleImage articleImage,
            String articleName,
            BigDecimal articlePrice) {

        String imageUrl = null;
        try {
            if (articleImage != null && chatRoom.getArticleId() != null) {
                imageUrl = articleImage.getImageUrl(chatRoom.getArticleId());
            }
        } catch (Exception e) {
            log.warn("상품 이미지 URL 조회 중 오류 발생: articleId={}, error={}",
                     chatRoom.getArticleId(), e.getMessage());
        }

        return ChatRoomResponseDTO.builder()
                .chatRoomId(chatRoom.getId())
                .wsRoomId(chatRoom.getWsRoomId())
                .articleId(chatRoom.getArticleId())
                .buyerId(chatRoom.getBuyer() != null ? chatRoom.getBuyer().getUserId() : null)
                .sellerId(chatRoom.getSeller() != null ? chatRoom.getSeller().getUserId() : null)
                .buyerNickname(users.getUserNicknameOrUnknown(chatRoom.getBuyer() != null ? chatRoom.getBuyer().getUserId() : null))
                .sellerNickname(users.getUserNicknameOrUnknown(chatRoom.getSeller() != null ? chatRoom.getSeller().getUserId() : null))
                .lastMessage(getLastMessage(chatRoom.getId(), lastMessages, chatRoom.getWsRoomId(), users))
                .createdAt(chatRoom.getCreatedAt())
                .updatedAt(chatRoom.getUpdatedAt())
                .articleImageUrl(imageUrl)
                .unreadMessageCount(unreadCounts.getUnreadCount(chatRoom.getId()))
                .buyerProfileImageUrl(users.getUserProfileImage(chatRoom.getBuyer() != null ? chatRoom.getBuyer().getUserId() : null))
                .sellerProfileImageUrl(users.getUserProfileImage(chatRoom.getSeller() != null ? chatRoom.getSeller().getUserId() : null))
                .articleName(articleName)
                .articlePrice(articlePrice)
                .build();
    }

    public ChatRoom validateChatRoomAccess(String wsRoomId, Long userId) {
        log.debug("채팅방 접근 권한 검증: wsRoomId={}, userId={}", wsRoomId, userId);

        // 1. 채팅방 조회
        ChatRoom chatRoom = findChatRoomByWsRoomId(wsRoomId);

        // 2. 채팅방 상태 및 사용자 권한 검증
        chatRoom.validateStatus();
        chatRoom.validateUserAccess(userId);

        // 3. 메타 정보 검증
        validateMetaInfoAccess(chatRoom.getId(), userId);

        return chatRoom;
    }

    /**
     * WebSocket 룸 ID로 채팅방을 조회합니다.
     *
     * @param wsRoomId WebSocket 룸 ID
     * @return 조회된 채팅방
     * @throws DuckwhoException 채팅방을 찾을 수 없는 경우
     */
    private ChatRoom findChatRoomByWsRoomId(String wsRoomId) {
        return chatRoomRepository.findByWsRoomIdWithParticipantsAndUsers(wsRoomId)
                .orElseThrow(() -> new DuckwhoException(ErrorCode.CHAT_ROOM_NOT_FOUND));
    }


    /**
     * 채팅방 메타 정보의 접근 권한을 검증합니다.
     *
     * @param chatRoomId 채팅방 ID
     * @param userId 사용자 ID
     * @throws DuckwhoException 메타 정보를 찾을 수 없거나 접근 권한이 없는 경우
     */
    private void validateMetaInfoAccess(Long chatRoomId, Long userId) {
        ChatRoomMetaInfo metaInfo = chatRoomMetaRepository.findByChatRoomId(chatRoomId)
                .orElseThrow(() -> new DuckwhoException(ErrorCode.CHAT_ROOM_NOT_FOUND));

        // 1. 기본 사용자 권한 검증
        metaInfo.validateUserAccess(userId);

        // 2. 사용자가 채팅방을 나갔는지 확인 (비활성 상태 체크)
        if (!metaInfo.getParticipants().isParticipantActive(userId)) {
            throw new DuckwhoException(ErrorCode.CHAT_ROOM_LEFT_USER);
        }
    }


    public Integer getTotalUnreadCount(Long userId) {
        List<ChatRoomMetaInfo> userChatrooms = chatRoomMetaRepository
                .findChatRoomMetaInfosByParticipantUserId(userId);

        if (userChatrooms.isEmpty()) {
            return 0;
        }

        return ChatRoomMetaInfo.calculateTotalUnreadCount(userChatrooms, userId);
    }


    public List<ChatRoomResponseDTO> findChatRoomListByRole(Long userId, JangterChatRole role) {
        List<ChatRoom> chatRooms = chatRoomRepository.findChatRoomsByUserIdAndRole(
                userId, role, ChatRoomStatus.ACTIVE);

        if (chatRooms.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> chatRoomIds = ChatRoom.extractChatRoomIds(chatRooms);
        List<Long> articleIds = ChatRoom.extractArticleIds(chatRooms);

        // 1. 상품 정보 조회
        ArticleInfo articleInfo = findArticleInfos(articleIds);

        // 2. 필요한 데이터 개별 준비
        ChatRoomMetaInfoData metaInfoData = getChatRoomMetaInfoData(chatRoomIds, userId);
        ChatRoomMetaInfos metaInfos = metaInfoData.getMetaInfos();
        ChatRoomMessages lastMessages = metaInfoData.getLastMessages();
        UnreadMessageCounts unreadCounts = metaInfoData.getUnreadCounts();
        ArticleImage articleImage = findArticleImage(articleIds);
        ChatRoomUsers users = ChatRoomUsers.fromChatRooms(chatRooms);

        // 3. DTO 변환
        return chatRooms.stream()
                .map(room -> {
                    // 각 채팅방에 필요한 상품 정보 제공
                    Long articleId = room.getArticleId();
                    String title = articleInfo.getTitle(articleId);
                    BigDecimal price = articleInfo.getPrice(articleId);

                    return createChatRoomResponseDTO(
                            room,
                            metaInfos.getMetaInfo(room.getId()).orElse(null),
                            users,
                            lastMessages,
                            unreadCounts,
                            articleImage,
                            title,
                            price
                    );
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }


    private ChatRoomMetaInfoData getChatRoomMetaInfoData(List<Long> chatRoomIds, Long userId) {
        if (chatRoomIds.isEmpty()) {
            return ChatRoomMetaInfoData.empty();
        }

        List<ChatRoomMetaInfo> metaInfos = chatRoomMetaRepository.findMetaInfoWithLastMessages(chatRoomIds);

        // 해당 사용자가 활성 상태인 채팅방만 필터링
        metaInfos = metaInfos.stream()
                .filter(metaInfo -> metaInfo.getParticipants().isParticipantActive(userId))
                .collect(Collectors.toList());

        if (metaInfos.isEmpty()) {
            return ChatRoomMetaInfoData.empty();
        }

        ChatRoomMessages lastMessages = ChatRoomMessages.fromMetaInfos(metaInfos);
        UnreadMessageCounts unreadCounts = UnreadMessageCounts.fromMetaInfos(metaInfos, userId, chatRoomIds);

        return new ChatRoomMetaInfoData(metaInfos, lastMessages, unreadCounts);
    }

    /**
     * 상품 이미지를 조회합니다.
     */
    private ArticleImage findArticleImage(List<Long> articleIds) {
        if (articleIds.isEmpty()) {
            return ArticleImage.empty();
        }

        // 중복 제거 및 null 필터링
        List<Long> distinctArticleIds = articleIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        if (distinctArticleIds.isEmpty()) {
            return ArticleImage.empty();
        }

        // 모든 상품 이미지 조회
        List<ProductImageDTO> productImages = duckuJangterRepository.findProductImagesById(distinctArticleIds);
        return ArticleImage.fromProductImageDTOs(productImages);
    }

    /**
     * 사용자의 모든 채팅방 목록을 조회합니다.
     *
     * @param userId 사용자 ID
     * @return 채팅방 응답 DTO 목록
     */
    public List<ChatRoomResponseDTO> findChatRoomList(Long userId) {
        Slice<ChatRoomResponseDTO> slice = findChatRoomListWithSlice(userId, Pageable.unpaged());
        return slice.getContent();
    }

    private ChatMessageResponseDTO getLastMessage(Long chatRoomId, ChatRoomMessages lastMessages, String wsRoomId, ChatRoomUsers users) {
        if (chatRoomId == null || lastMessages == null) {
            return null;
        }

        try {
            Optional<ChatMessage> messageOpt = lastMessages.getLastMessage(chatRoomId);
            if (messageOpt.isEmpty()) {
                return null;
            }

            ChatMessage lastMessage = messageOpt.get();
            if (lastMessage == null || lastMessage.getSenderId() == null) {
                return null;
            }

            String senderName = users != null
                ? users.getUserNicknameOrUnknown(lastMessage.getSenderId())
                : MessageConstants.UNKNOWN_USER;

            return ChatMessageResponseDTO.from(lastMessage, senderName, wsRoomId);
        } catch (Exception e) {
            log.warn("마지막 메시지 조회 중 오류 발생: chatRoomId={}, error={}", chatRoomId, e.getMessage());
            return null;
        }
    }

    /**
     * 상품 ID 목록으로 상품 정보를 효율적으로 조회합니다.
     * QueryDSL 기반의 Projection을 활용하여 필요한 정보만 가져옵니다.
     */
    private ArticleInfo findArticleInfos(List<Long> articleIds) {
        if (articleIds == null || articleIds.isEmpty()) {
            return ArticleInfo.empty();
        }

        List<ArticleInfoDTO> articleInfoDTOs = duckuJangterRepository.findArticleInfosByIds(articleIds);
        return ArticleInfo.from(articleInfoDTOs);
    }

}