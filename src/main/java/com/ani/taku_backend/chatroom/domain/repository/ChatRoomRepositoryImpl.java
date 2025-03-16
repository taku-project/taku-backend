package com.ani.taku_backend.chatroom.domain.repository;

import com.ani.taku_backend.chatroom.domain.constant.ChatRoomStatus;
import com.ani.taku_backend.chatroom.domain.constant.JangterChatRole;
import com.ani.taku_backend.chatroom.domain.dto.ChatRoomDetailDTO;
import com.ani.taku_backend.chatroom.domain.entity.ChatRoom;
import com.ani.taku_backend.chatroom.domain.entity.QChatRoom;
import com.ani.taku_backend.chatroom.domain.entity.QChatRoomParticipant;
import com.ani.taku_backend.user.model.entity.QUser;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;

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
    public List<ChatRoom> findChatRoomsWithParticipantsAndUsers(Long userId, ChatRoomStatus status) {
        log.debug("사용자 ID: {}, 상태: {}로 채팅방 조회 시작", userId, status);
        
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
                .fetch();

        if (chatRoomIds.isEmpty()) {
            log.debug("사용자가 참여한 채팅방이 없습니다: userId={}", userId);
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
    public List<ChatRoomDetailDTO> findChatRoomsWithAllDetails(Long userId, ChatRoomStatus status) {
        
        QChatRoom chatRoom = QChatRoom.chatRoom;
        QChatRoomParticipant participant = QChatRoomParticipant.chatRoomParticipant;
        QChatRoomParticipant buyerParticipant = new QChatRoomParticipant("buyerParticipant");
        QChatRoomParticipant sellerParticipant = new QChatRoomParticipant("sellerParticipant");
        QUser buyer = new QUser("buyer");
        QUser seller = new QUser("seller");

        List<ChatRoomDetailDTO> results = queryFactory
                .select(Projections.constructor(ChatRoomDetailDTO.class,
                        chatRoom.id,
                        chatRoom.wsRoomId,
                        chatRoom.articleId,
                        chatRoom.createdAt,
                        buyer.userId,
                        buyer.nickname,
                        buyer.profileImg,
                        seller.userId,
                        seller.nickname,
                        seller.profileImg,
                        null, // lastMessage는 MongoDB에서 가져와야 함
                        null, // lastMessageSentAt
                        null, // lastMessageSenderId
                        null  // unreadCount
                ))
                .from(chatRoom)
                .join(chatRoom.participants, participant).on(participant.user.userId.eq(userId))
                .join(chatRoom.participants, buyerParticipant).on(buyerParticipant.role.eq(JangterChatRole.BUYER))
                .join(buyerParticipant.user, buyer)
                .join(chatRoom.participants, sellerParticipant).on(sellerParticipant.role.eq(JangterChatRole.SELLER))
                .join(sellerParticipant.user, seller)
                .where(
                    chatRoom.status.eq(status)
                )
                .fetch();

        
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
}