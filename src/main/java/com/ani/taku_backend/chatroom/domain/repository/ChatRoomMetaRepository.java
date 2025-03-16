package com.ani.taku_backend.chatroom.domain.repository;

import com.ani.taku_backend.chatroom.domain.document.ChatRoomMetaInfo;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomMetaRepository extends MongoRepository<ChatRoomMetaInfo, String>, ChatRoomMetaRepositoryCustom {

    /**
     * 채팅방 ID로 메타정보 조회
     */
    Optional<ChatRoomMetaInfo> findByChatRoomId(Long chatRoomId);

    /**
     * 채팅방 ID 목록으로 메타정보 조회
     */
    List<ChatRoomMetaInfo> findByChatRoomIdIn(List<Long> chatRoomIds);

    /**
     * 사용자가 참여한 모든 채팅방 메타정보 조회
     */
    @Query(value = "{'participants.info.?0': {$exists: true}}")
    List<ChatRoomMetaInfo> findChatRoomMetaInfosByParticipantUserId(Long userId);

}