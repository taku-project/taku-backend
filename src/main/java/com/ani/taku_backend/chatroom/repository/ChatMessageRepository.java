package com.ani.taku_backend.chatroom.repository;

import com.ani.taku_backend.chatroom.model.entity.ChatMessage;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {
    // 특정 roomId에 해당하는 메시지들을 가져오기
    List<ChatMessage> findByRoomId(Long roomId);
}
