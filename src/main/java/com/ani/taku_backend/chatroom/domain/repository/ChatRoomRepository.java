package com.ani.taku_backend.chatroom.domain.repository;

import com.ani.taku_backend.chatroom.domain.entity.ChatRoom;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long>, ChatRoomRepositoryCustom {

    /**
     * WebSocket 세션 관리를 위한 roomId로 채팅방을 조회합니다.
     */
    Optional<ChatRoom> findByWsRoomId(String roomId);

    //ChatRoomId로 채팅방 찾기
    Optional<ChatRoom> findById(Long id);

}