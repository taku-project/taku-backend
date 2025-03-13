package com.ani.taku_backend.chatroom.domain.repository;

import com.ani.taku_backend.chatroom.domain.constant.MarketRole;
import com.ani.taku_backend.chatroom.domain.document.ParticipantInfo;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ParticipantInfoRepository extends MongoRepository<ParticipantInfo, Long> {
    ParticipantInfo findByRole(MarketRole role);
}
