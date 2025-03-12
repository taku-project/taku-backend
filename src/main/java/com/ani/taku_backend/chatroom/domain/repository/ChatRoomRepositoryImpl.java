package com.ani.taku_backend.chatroom.domain.repository;

import com.ani.taku_backend.chatroom.domain.constant.ChatRoomStatus;
import com.ani.taku_backend.chatroom.domain.dto.ChatRoomDetailDTO;
import com.ani.taku_backend.chatroom.domain.entity.ChatRoom;
import com.ani.taku_backend.chatroom.domain.entity.QChatRoom;
import com.ani.taku_backend.chatroom.domain.document.ChatRoomMetaInfo;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;


@Repository
@RequiredArgsConstructor
public class ChatRoomRepositoryImpl implements ChatRoomRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final ChatRoomMetaRepository chatRoomMetaRepository;

    /**
     * 사용자의 채팅방 목록을 상세 정보와 함께 조회합니다.
     *
     * @param userId 사용자 ID
     * @param status 채팅방 상태
     * @return 채팅방 상세 정보 DTO 리스트
     */
    @Override
    public List<ChatRoomDetailDTO> findChatRoomsWithDetailsForUser(Long userId, ChatRoomStatus status) {
        QChatRoom chatRoom = QChatRoom.chatRoom;
        
        // 1. MongoDB에서 사용자가 참여한 채팅방 메타정보 목록 조회
        List<ChatRoomMetaInfo> chatRoomMetaInfos = chatRoomMetaRepository.findChatRoomMetaInfosByParticipantUserId(userId);
        
        // 메타정보에서 채팅방 ID만 추출
        List<Long> activeChatRoomIds = chatRoomMetaInfos.stream()
            .map(ChatRoomMetaInfo::getChatRoomId)
            .collect(Collectors.toList());
        
        if (activeChatRoomIds.isEmpty()) {
            return List.of();
        }

        // 2. JPA를 통해 채팅방 기본 정보 조회
        List<ChatRoom> rooms = queryFactory
                .selectFrom(chatRoom)
                .where(chatRoom.id.in(activeChatRoomIds)
                        .and(chatRoom.status.eq(status)))
                .orderBy(chatRoom.createdAt.desc())
                .fetch();
        
        if (rooms.isEmpty()) {
            return List.of();
        }

        return rooms.stream()
                .map(room -> {
                    // 메타정보에서 해당 채팅방의 안 읽은 메시지 수 조회
                    int unreadCount = 0;
                    for (ChatRoomMetaInfo meta : chatRoomMetaInfos) {
                        if (meta.getChatRoomId().equals(room.getId())) {
                            unreadCount = meta.getUnreadCount(userId);
                            break;
                        }
                    }
                    
                    // 빌더 패턴으로 DTO 생성
                    return ChatRoomDetailDTO.builder()
                        .id(room.getId())
                        .roomId(room.getWsRoomId())
                        .articleId(room.getArticleId())
                        .createdAt(room.getCreatedAt())
                        .unreadCount(unreadCount)
                        .build();
                })
                .collect(Collectors.toList());
    }
} 