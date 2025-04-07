package com.ani.taku_backend.chatroom.domain.repository;

import com.ani.taku_backend.chatroom.domain.constant.ChatRoomStatus;
import com.ani.taku_backend.chatroom.domain.constant.JangterChatRole;
import com.ani.taku_backend.chatroom.domain.entity.ChatRoom;
import com.ani.taku_backend.chatroom.domain.entity.QChatRoom;
import com.ani.taku_backend.chatroom.domain.entity.QChatRoomParticipant;
import com.ani.taku_backend.user.model.entity.QUser;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class ChatRoomRepositoryImpl implements ChatRoomRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;
    
    private JPAQueryFactory queryFactory;
    
    public ChatRoomRepositoryImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
        this.queryFactory = new JPAQueryFactory(entityManager);
    }


    @Override
    public List<ChatRoom> findChatRoomsByUserIdAndRole(Long userId, JangterChatRole role, ChatRoomStatus status) {
        log.debug("사용자 ID: {}, 역할: {}, 상태: {}로 채팅방 조회 시작", userId, role, status);
        
        QChatRoom chatRoom = QChatRoom.chatRoom;
        QChatRoomParticipant participant = QChatRoomParticipant.chatRoomParticipant;
        QUser user = QUser.user;


        List<Long> chatRoomIds = queryFactory
                .select(participant.chatRoom.id)
                .from(participant)
                .where(
                    participant.user.userId.eq(userId),
                    participant.role.eq(role),
                    participant.chatRoom.status.eq(status)
                )
                .fetch();

        if (chatRoomIds.isEmpty()) {
            log.debug("사용자의 역할에 해당하는 채팅방이 없습니다: userId={}, role={}", userId, role);
            return Collections.emptyList();
        }

        List<ChatRoom> results = queryFactory
                .selectFrom(chatRoom)
                .distinct()
                .leftJoin(chatRoom.participants, participant).fetchJoin()
                .leftJoin(participant.user, user).fetchJoin()
                .where(chatRoom.id.in(chatRoomIds))
                .fetch();

        log.debug("조회된 채팅방 수: {}", results.size());
        
        return results;
    }

    @Override
    public Optional<ChatRoom> findByWsRoomIdWithParticipantsAndUsers(String wsRoomId) {
        log.debug("WebSocket 채팅방 ID: {}로 채팅방 조회 시작", wsRoomId);
        
        QChatRoom chatRoom = QChatRoom.chatRoom;
        QChatRoomParticipant participant = QChatRoomParticipant.chatRoomParticipant;
        QUser user = QUser.user;

        ChatRoom result = queryFactory
                .selectFrom(chatRoom)
                .distinct()
                .leftJoin(chatRoom.participants, participant).fetchJoin()
                .leftJoin(participant.user, user).fetchJoin()
                .where(chatRoom.wsRoomId.eq(wsRoomId))
                .fetchOne();

        log.debug("채팅방 조회 결과: {}", result != null ? "성공" : "실패");
        
        return Optional.ofNullable(result);
    }
    
    @Override
    public Slice<ChatRoom> findChatRoomsWithSlice(Long userId, Pageable pageable, ChatRoomStatus status) {
        log.debug("사용자 ID: {}, 페이지: {}, 크기: {}, 상태: {}로 채팅방 페이징 조회 시작", 
                userId, pageable.getPageNumber(), pageable.getPageSize(), status);
        
        QChatRoom chatRoom = QChatRoom.chatRoom;
        QChatRoomParticipant participant = QChatRoomParticipant.chatRoomParticipant;
        QUser user = QUser.user;

        List<Long> chatRoomIds = queryFactory
                .select(participant.chatRoom.id)
                .from(participant)
                .where(
                    participant.user.userId.eq(userId),
                    participant.chatRoom.status.eq(status)
                )
                .orderBy(chatRoom.updatedAt.desc(), chatRoom.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize() + 1) // 다음 페이지 여부를 확인하기 위해 1개 더 요청
                .fetch();

        if (chatRoomIds.isEmpty()) {
            log.debug("조회할 채팅방이 없습니다: userId={}", userId);
            return new SliceImpl<>(Collections.emptyList(), pageable, false);
        }

        List<ChatRoom> results = queryFactory
                .selectFrom(chatRoom)
                .distinct()
                .leftJoin(chatRoom.participants, participant).fetchJoin()
                .leftJoin(participant.user, user).fetchJoin()
                .where(chatRoom.id.in(chatRoomIds))
                .orderBy(chatRoom.updatedAt.desc(), chatRoom.id.desc())
                .fetch();

        log.debug("페이징 조회된 채팅방 수: {}", results.size());

        boolean hasNext = results.size() > pageable.getPageSize();

        if (hasNext) {
            results = results.subList(0, pageable.getPageSize());
        }
        
        return new SliceImpl<>(results, pageable, hasNext);
    }
}