
package com.ani.taku_backend.chatroom.repository;

import com.ani.taku_backend.chatroom.model.document.ChatRoomMetaInfo;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ChatroomMetaRepository extends MongoRepository<ChatRoomMetaInfo, Long > {
    @Query("{ 'participants.info.?0': { $exists: true } }")
    List<ChatRoomMetaInfo> findByParticipantIdOrderByUpdateAtDesc(String userId);

    @Query("{ 'participants.info.?0': {$exists: true} }")
    List<ChatRoomMetaInfo> findByParticipantsUserId(Long userId);


}