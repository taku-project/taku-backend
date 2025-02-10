package com.ani.taku_backend.chatroom.repository;

import com.ani.taku_backend.chatroom.model.constant.ParticipantRole;
import com.ani.taku_backend.chatroom.model.document.ParticipantInfo;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ParticipantInfoRepository extends MongoRepository<ParticipantInfo, Long> {

    ParticipantInfo findByRole(ParticipantRole role);

}
