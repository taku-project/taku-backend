package com.ani.taku_backend.chatroom.domain.repository;

import com.ani.taku_backend.chatroom.domain.document.ChatMessage;

import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.domain.Limit;
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

    // 무한 스크롤을 위한 메소드
    // 1. 최신 메시지부터 limit 개수만큼 가져오기 (첫 로드)
    List<ChatMessage> findByChatRoomIdOrderBySentAtDesc(Long chatRoomId, Limit limit);

    // 2. 특정 시간보다 이전 메시지 limit 개수만큼 가져오기 (스크롤 시)
    List<ChatMessage> findByChatRoomIdAndSentAtBeforeOrderBySentAtDesc(
            Long chatRoomId,
            LocalDateTime sentAt,
            Limit limit
    );

    /**
     * 여러 채팅방의 마지막 메시지를 한 번에 조회하는 메서드
     * 각 채팅방에 대해 가장 최근 메시지를 하나씩 반환합니다.
     * 
     * @param chatRoomIds 채팅방 ID 목록
     * @return 각 채팅방의 마지막 메시지 목록
     */
    @Query(value = "{ 'chatRoomId': { $in: ?0 } }", sort = "{ 'sentAt': -1 }")
    List<ChatMessage> findLatestMessagesByChatRoomIds(List<Long> chatRoomIds);
}
