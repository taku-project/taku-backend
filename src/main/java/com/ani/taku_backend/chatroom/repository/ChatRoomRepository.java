package com.ani.taku_backend.chatroom.repository;

import com.ani.taku_backend.chatroom.model.constant.ChatRoomStatus;
import com.ani.taku_backend.chatroom.model.entity.ChatRoom;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    /**
     * 특정 구매자의 활성화된 채팅방 목록을 조회합니다.
     * 생성일시 기준 내림차순으로 정렬됩니다.
     */

    List<ChatRoom> findByIdInAndStatus(List<Long> id, ChatRoomStatus status);
    /**
     * WebSocket 세션 관리를 위한 roomId로 채팅방을 조회합니다.
     */
    Optional<ChatRoom> findByWsRoomId(String roomId);

    //ChatRoomId로 채팅방 찾기
    Optional<ChatRoom> findById(Long id);

    // 판매글 ID와 구매자, 판매자 ID로 채팅방을 찾기
    List<ChatRoom> findByArticleId(Long articleId);

    /**
     * 채팅방 ID 목록을 기반으로 필요한 필드만 조회하는 최적화된 메서드
     * 
     * @param chatRoomIds 채팅방 ID 목록
     * @param status 채팅방 상태
     * @return 필요한 필드만 포함된 채팅방 목록
     */
    @Query("SELECT c FROM ChatRoom c WHERE c.id IN :chatRoomIds AND c.status = :status")
    List<ChatRoom> findByIdInAndStatusOptimized(List<Long> chatRoomIds, ChatRoomStatus status);

}