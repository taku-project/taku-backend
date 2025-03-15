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
import java.util.stream.Collectors;


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
        log.info("사용자의 모든 채팅방 검색 시작: userId={}, status={}", userId, status);
        QChatRoom chatRoom = QChatRoom.chatRoom;
        QChatRoomParticipant participant = QChatRoomParticipant.chatRoomParticipant;
        QUser user = QUser.user;

        // 쿼리 1: 사용자가 참여한 채팅방 ID만 먼저 가져오기
        List<Long> chatRoomIds = queryFactory
                .select(participant.chatRoom.id)
                .from(participant)
                .where(
                    participant.user.userId.eq(userId),
                    participant.chatRoom.status.eq(status)
                )
                .fetch();

        if (chatRoomIds.isEmpty()) {
            log.info("참여 중인 채팅방이 없습니다: userId={}", userId);
            return Collections.emptyList();
        }

        log.info("사용자 채팅방 ID 검색 결과: {} 개 찾음", chatRoomIds.size());

        // 쿼리 2: ID 기반으로 채팅방과 관련 엔티티를 페치 조인으로 가져오기
        List<ChatRoom> results = queryFactory
                .selectDistinct(chatRoom)
                .from(chatRoom)
                .leftJoin(chatRoom.participants, participant).fetchJoin()
                .leftJoin(participant.user, user).fetchJoin()
                .where(chatRoom.id.in(chatRoomIds))
                .fetch();

        log.info("채팅방 상세 정보 로드 완료: {} 개의 채팅방", results.size());

        return results;
    }

    @Override
    public List<ChatRoom> findChatRoomsByUserIdAndRole(Long userId, JangterChatRole role, ChatRoomStatus status) {
        log.info("사용자 롤별 채팅방 검색 시작: userId={}, role={}, status={}", userId, role, status);
        QChatRoom chatRoom = QChatRoom.chatRoom;
        QChatRoomParticipant participant = QChatRoomParticipant.chatRoomParticipant;
        QUser user = QUser.user;

        // 쿼리 1: 사용자가 특정 역할로 참여한 채팅방 ID만 먼저 가져오기
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
            log.info("해당 역할로 참여 중인 채팅방이 없습니다: userId={}, role={}", userId, role);
            return Collections.emptyList();
        }

        log.info("사용자 롤별 채팅방 ID 검색 결과: {} 개 찾음", chatRoomIds.size());

        // 쿼리 2: ID 기반으로 채팅방과 관련 엔티티를 페치 조인으로 가져오기
        List<ChatRoom> results = queryFactory
                .selectDistinct(chatRoom)
                .from(chatRoom)
                .leftJoin(chatRoom.participants, participant).fetchJoin()
                .leftJoin(participant.user, user).fetchJoin()
                .where(chatRoom.id.in(chatRoomIds))
                .fetch();

        if (log.isDebugEnabled()) {
            for (ChatRoom room : results) {
                log.debug("채팅방 정보: id={}, 참가자 수={}, 구매자={}, 판매자={}", 
                        room.getId(), 
                        room.getParticipants().size(),
                        room.getParticipants().stream()
                            .filter(p -> p.isBuyer())
                            .map(p -> p.getUser().getUserId().toString())
                            .collect(Collectors.joining(",")),
                        room.getParticipants().stream()
                            .filter(p -> p.isSeller())
                            .map(p -> p.getUser().getUserId().toString())
                            .collect(Collectors.joining(",")));
            }
        }

        log.info("채팅방 상세 정보 로드 완료: {} 개의 채팅방", results.size());

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

        return queryFactory
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
    }

    @Override
    public Optional<ChatRoom> findByWsRoomIdWithParticipantsAndUsers(String wsRoomId) {
        QChatRoom chatRoom = QChatRoom.chatRoom;
        QChatRoomParticipant participant = QChatRoomParticipant.chatRoomParticipant;
        QUser user = QUser.user;

        ChatRoom result = queryFactory
                .selectDistinct(chatRoom)
                .from(chatRoom)
                .join(chatRoom.participants, participant).fetchJoin()
                .join(participant.user, user).fetchJoin()
                .where(chatRoom.wsRoomId.eq(wsRoomId))
                .fetchOne();

        return Optional.ofNullable(result);
    }
}