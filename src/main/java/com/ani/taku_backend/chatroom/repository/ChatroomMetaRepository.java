package com.ani.taku_backend.chatroom.repository;

import com.ani.taku_backend.chatroom.model.document.ChatroomMetaInfo;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ChatroomMetaRepository extends MongoRepository<ChatroomMetaInfo, String> {
    /**
     * 사용자가 참여한 모든 채팅방의 메타 정보를 조회합니다.
     * 업데이트 시간 기준 내림차순으로 정렬됩니다.
     */
    @Query(value = "{ 'participants.info.?0': { $exists: true } }",
            sort = "{ 'updateAt': -1 }")
    List<ChatroomMetaInfo> findByParticipantIdOrderByUpdateAtDesc(String userId);

    /**
     * 채팅방 목록에 대한 메타 정보를 일괄 조회합니다.
     * 여러 채팅방의 메타 정보를 단일 쿼리로 조회하여 N+1 문제를 방지합니다.
     */
    @Query("{ '_id': { $in: ?0 } }")
    List<ChatroomMetaInfo> findAllByRoomIds(List<String> roomIds);
}