package com.ani.taku_backend.chatroom.repository;

import com.ani.taku_backend.chatroom.model.document.ChatMessage;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {
    // 특정 roomId에 해당하는 메시지들을 가져오기
    List<ChatMessage> findByChatRoomId(Long roomId);

    // 마지막으로 전송된 메시지 찾기 (시간 내림차순 정렬)
    Optional<ChatMessage> findTopByChatRoomIdOrderBySentAtDesc(Long chatRoomId);

    // 읽지 않은 메시지들을 일괄 업데이트
    @Update("{ '$set': { 'read': true } }")
    @Query("{ 'chatRoomId': ?0, 'senderId': { $ne: ?1 }, 'read': false }")
    void updateReadStatusForMessages(Long chatRoomId, Long userId);
}
