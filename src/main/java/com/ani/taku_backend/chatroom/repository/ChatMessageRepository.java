package com.ani.taku_backend.chatroom.repository;

import com.ani.taku_backend.chatroom.model.document.ChatMessage;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {
    // 특정 roomId에 해당하는 메시지들을 가져오기
    List<ChatMessage> findByChatRoomId(Long roomId);

    // 읽지 않은 메시지들을 일괄 업데이트
    @Update("{ '$set': { 'read': true } }")
    @Query("{ 'chatRoomId': ?0, 'senderId': { $ne: ?1 }, 'read': false }")
    void updateReadStatusForMessages(Long chatRoomId, Long userId);
}
