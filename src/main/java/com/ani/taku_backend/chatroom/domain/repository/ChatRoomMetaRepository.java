package com.ani.taku_backend.chatroom.domain.repository;

import com.ani.taku_backend.chatroom.domain.document.ChatRoomMetaInfo;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomMetaRepository extends MongoRepository<ChatRoomMetaInfo, String> {
    @Query("{ 'participants.info.?0': { $exists: true } }")
    List<ChatRoomMetaInfo> findByParticipantIdOrderByUpdateAtDesc(String userId);

    @Query("{ 'participants.info.?0': {$exists: true} }")
    List<ChatRoomMetaInfo> findByParticipantsUserId(Long userId);

    Optional<ChatRoomMetaInfo> findByChatRoomId(Long chatRoomId);

    List<ChatRoomMetaInfo> findByChatRoomIdIn(List<Long> chatRoomIds);

    @Update("{ '$set': { 'participants.info.?1.messageStock': 0 } }")
    @Query("{ 'chatRoomId': ?0, 'participants.info.?1': { $exists: true } }")
    void resetMessageStock(Long chatRoomId, Long userId);

    /**
     * 사용자 ID를 기반으로 활성 상태인 채팅방 메타 정보만 조회하는 최적화된 메서드
     * 
     * @param userId 사용자 ID
     * @return 활성 상태인 채팅방 메타 정보 목록
     */
    @Query("{ 'participants.info.?0.isConnected': true }")
    List<ChatRoomMetaInfo> findActiveByParticipantsUserId(String userId);
}