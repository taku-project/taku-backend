package com.ani.taku_backend.chatroom.repository;

import com.ani.taku_backend.chatroom.model.document.ChatRoomMetaInfo;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
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
}