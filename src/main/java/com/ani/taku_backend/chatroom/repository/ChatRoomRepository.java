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

    List<ChatRoom> findByIdInAndStatus(List<Long> id, ChatRoomStatus status);
    /**
     * WebSocket 세션 관리를 위한 roomId로 채팅방을 조회합니다.
     */
    Optional<ChatRoom> findByWsRoomId(String roomId);

    /**
     * 동일한 상품에 대해 동일한 구매자와 판매자 간의 채팅방 존재 여부를 확인합니다.
     * 중복 채팅방 생성을 방지하기 위해 사용됩니다.
     */
    boolean existsByArticleId(Long articleId);


    //ChatRoomId로 채팅방 찾기
    Optional<ChatRoom> findById(Long id);

    // 판매글 ID와 구매자, 판매자 ID로 채팅방을 찾기
    List<ChatRoom> findByArticleId(Long articleId);



}