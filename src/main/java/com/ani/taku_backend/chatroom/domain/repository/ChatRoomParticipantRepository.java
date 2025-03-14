package com.ani.taku_backend.chatroom.domain.repository;

import com.ani.taku_backend.chatroom.domain.entity.ChatRoomParticipant;
import com.ani.taku_backend.chatroom.domain.constant.MarketRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface ChatRoomParticipantRepository extends JpaRepository<ChatRoomParticipant, Long> {

    /**
     * 특정 사용자가 특정 역할로 참여한 채팅방의 참여자 정보를 조회합니다.
     *
     * @param userId 사용자 ID
     * @param role 역할 (BUYER 또는 SELLER)
     * @return 참여자 정보 목록
     */
    @Query("SELECT p FROM ChatRoomParticipant p WHERE p.user.userId = :userId AND p.role = :role")
    List<ChatRoomParticipant> findByUserIdAndRole(@Param("userId") Long userId, @Param("role") MarketRole role);

}