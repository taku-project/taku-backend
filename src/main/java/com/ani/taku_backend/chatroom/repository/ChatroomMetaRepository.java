
package com.ani.taku_backend.chatroom.repository;

import com.ani.taku_backend.chatroom.model.document.ChatroomMetaInfo;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ChatroomMetaRepository extends MongoRepository<ChatroomMetaInfo, String> {
    @Query("{ 'participants.info.?0': { $exists: true } }")
    List<ChatroomMetaInfo> findByParticipantIdOrderByUpdateAtDesc(String userId);
}