package com.ani.taku_backend.chatroom.domain.repository;

import com.ani.taku_backend.chatroom.domain.document.ChatRoomMetaInfo;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;
import org.springframework.data.repository.query.Param;
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
     * 채팅방 메타정보 목록에서 채팅방 ID 목록을 추출합니다.
     * @param userId 사용자 ID
     * @return 채팅방 ID 목록
     */
    @Query(value = "{'participants.info.$userId.isConnected': true}", fields = "{'chatRoomId': 1, '_id': 0}")
    List<Long> findChatRoomIdsByUserId(@Param("userId") Long userId);

    /**
     * 참여자 ID로 해당 사용자가 참여한 채팅방 ID 목록을 조회합니다.
     * 연결 상태와 관계없이 모든 채팅방을 조회합니다.
     * 
     * @param userId 참여자 ID
     * @return 채팅방 ID 목록
     */
    @Query(value = "{'participants.info.?0': {$exists: true}}", fields = "{'chatRoomId': 1, '_id': 0}")
    List<Long> findChatRoomIdsByParticipantUserId(Long userId);

    /**
     * 채팅방 ID 목록에 해당하는 모든 참여자 ID를 추출합니다.
     * @param chatRoomIds 채팅방 ID 목록
     * @return 참여자 ID 목록
     */
    @Query(value = "{'chatRoomId': {$in: ?0}}", fields = "{'participants.info': 1, '_id': 0}")
    List<Long> findAllParticipantIdsByChatRoomIds(List<Long> chatRoomIds);
}