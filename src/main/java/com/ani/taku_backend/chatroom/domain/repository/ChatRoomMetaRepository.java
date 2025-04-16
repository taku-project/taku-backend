package com.ani.taku_backend.chatroom.domain.repository;

import com.ani.taku_backend.chatroom.domain.document.ChatRoomMetaInfo;
import com.ani.taku_backend.chatroom.domain.document.ChatMessage;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Repository
public interface ChatRoomMetaRepository extends MongoRepository<ChatRoomMetaInfo, String>, ChatRoomMetaRepositoryCustom {

    /**
     * 채팅방 ID로 메타정보 조회
     */
    Optional<ChatRoomMetaInfo> findByChatRoomId(Long chatRoomId);

    /**
     * 사용자가 참여한 모든 채팅방 메타정보 조회
     */
    @Query(value = "{'participants.info.?0': {$exists: true}}")
    List<ChatRoomMetaInfo> findChatRoomMetaInfosByParticipantUserId(Long userId);

    /**
     * 특정 채팅방의 특정 시간 이전 메시지를 최신순으로 조회합니다. (무한 스크롤용)
     * 
     * @param chatRoomId 채팅방 ID
     * @param sentAt 이 시간 이전의 메시지만 조회
     * @param pageable 페이징 정보 (정렬, 제한)
     * @return 조건에 맞는 메시지 목록
     */
    @Query(value = "{ 'chatRoomId': ?0 }", fields = "{ 'messages': { $elemMatch: { 'sentAt': { $lt: ?1 } } } }")
    List<ChatMessage> findMessagesByChatRoomIdAndSentAtBeforeOrderBySentAtDesc(
        @Param("chatRoomId") Long chatRoomId,
        @Param("sentAt") LocalDateTime sentAt, 
        @Param("pageable") Pageable pageable
    );
}