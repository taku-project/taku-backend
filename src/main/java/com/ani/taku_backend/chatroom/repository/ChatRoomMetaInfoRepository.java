package com.ani.taku_backend.chatroom.repository;

import com.ani.taku_backend.chatroom.model.document.ChatRoomMetaInfo;
import org.springframework.data.mongodb.repository.MongoRepository;


public interface ChatRoomMetaInfoRepository extends MongoRepository<ChatRoomMetaInfo, Long> {
}
