package com.ani.taku_backend.chatroom.repository;

import com.ani.taku_backend.chatroom.model.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    /**
     * 사용자가 참여한 모든 채팅방과 참여자 정보를 단일 쿼리로 조회합니다.
     * - 생성일시 기준 내림차순 정렬
     */
    @Query("""
        SELECT DISTINCT cr FROM ChatRoom cr
        JOIN FETCH User buyer ON buyer.userId = cr.buyerId
        JOIN FETCH User seller ON seller.userId = cr.sellerId
        WHERE cr.buyerId = :userId OR cr.sellerId = :userId
        ORDER BY cr.createdAt DESC
        """)
    List<ChatRoom> findAllByUserIdWithParticipants(@Param("userId") Long userId);

    /**
     * 특정 채팅방과 참여자 정보를 단일 쿼리로 조회합니다.
     */
    @Query("""
        SELECT cr FROM ChatRoom cr
        JOIN FETCH User buyer ON buyer.userId = cr.buyerId
        JOIN FETCH User seller ON seller.userId = cr.sellerId
        WHERE cr.roomId = :roomId
        """)
    Optional<ChatRoom> findByRoomIdWithParticipants(@Param("roomId") String roomId);

    /**
     * WebSocket 세션 관리를 위한 roomId로 채팅방을 조회합니다.
     * 참여자 정보가 필요하지 않은 경우에 사용됩니다.
     */
    Optional<ChatRoom> findByRoomId(String roomId);

    /**
     * 동일한 상품에 대해 동일한 구매자와 판매자 간의 채팅방 존재 여부를 확인합니다.
     * 중복 채팅방 생성을 방지하기 위해 사용됩니다.
     */
    boolean existsByArticleIdAndBuyerIdAndSellerId(Long articleId, Long buyerId, Long sellerId);
}