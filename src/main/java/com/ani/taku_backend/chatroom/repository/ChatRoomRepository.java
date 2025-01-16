package com.ani.taku_backend.chatroom.repository;

import com.ani.taku_backend.chatroom.model.constant.ChatRoomStatus;
import com.ani.taku_backend.chatroom.model.entity.ChatRoom;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    /**
     * 특정 구매자의 활성화된 채팅방 목록을 조회합니다.
     * 생성일시 기준 내림차순으로 정렬됩니다.
     */

    List<ChatRoom> findByStatusAndBuyerIdOrderByCreatedAtDesc(ChatRoomStatus status, Long buyerId);

    /**
     * 특정 판매자의 활성화된 채팅방 목록을 조회합니다.
     * 생성일시 기준 내림차순으로 정렬됩니다.
     */
    List<ChatRoom> findByStatusAndSellerIdOrderByCreatedAtDesc(ChatRoomStatus status, Long sellerId);

    /**
     * WebSocket 세션 관리를 위한 roomId로 채팅방을 조회합니다.
     */
    Optional<ChatRoom> findByRoomId(String roomId);

    /**
     * 동일한 상품에 대해 동일한 구매자와 판매자 간의 채팅방 존재 여부를 확인합니다.
     * 중복 채팅방 생성을 방지하기 위해 사용됩니다.
     */
    boolean existsByArticleIdAndBuyerIdAndSellerId(Long articleId, Long buyerId, Long sellerId);
}