package com.ani.taku_backend.chatroom.domain.repository;

import com.ani.taku_backend.chatroom.domain.constant.ChatRoomStatus;
import com.ani.taku_backend.chatroom.domain.dto.ChatRoomDetailDTO;
import com.ani.taku_backend.chatroom.domain.entity.ChatRoom;
import com.ani.taku_backend.chatroom.domain.entity.QChatRoom;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;


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
        
        // 1. MongoDB에서 사용자가 참여한 채팅방 ID 목록 조회
        List<Long> activeChatRoomIds = chatRoomMetaRepository.findChatRoomIdsByParticipantUserId(userId);
        
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
                .map(room -> new ChatRoomDetailDTO(
                    room.getId(),
                    room.getWsRoomId(),
                    room.getArticleId(),
                    room.getCreatedAt(),
                    null, // 구매자 ID
                    null, // 구매자 닉네임
                    null, // 구매자 프로필 이미지
                    null, // 판매자 ID
                    null, // 판매자 닉네임
                    null, // 판매자 프로필 이미지
                    null, // 마지막 메시지
                    null, // 마지막 메시지 시간
                    null, // 마지막 메시지 발신자 ID
                    0    // 안읽은 메시지 수
                ))
                .toList();
    }
} 