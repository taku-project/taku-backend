package com.ani.taku_backend.chatroom.domain.repository;

import com.ani.taku_backend.chatroom.domain.entity.ChatRoomParticipant;
import com.ani.taku_backend.chatroom.domain.constant.MarketRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomParticipantRepository extends JpaRepository<ChatRoomParticipant, Long> {

    /**
     * 채팅방 ID와 사용자 ID로 참여자 정보를 조회합니다.
     *
     * @param chatRoomId 채팅방 ID
     * @param userId 사용자 ID
     * @return 참여자 정보 (Optional)
     */
    @Query("SELECT p FROM ChatRoomParticipant p WHERE p.chatRoom.id = :chatRoomId AND p.user.userId = :userId")
    Optional<ChatRoomParticipant> findByChatRoomIdAndUserId(@Param("chatRoomId") Long chatRoomId, @Param("userId") Long userId);

    /**
     * 채팅방 ID로 모든 참여자 정보를 조회합니다.
     *
     * @param chatRoomId 채팅방 ID
     * @return 참여자 정보 목록
     */
    @Query("SELECT p FROM ChatRoomParticipant p WHERE p.chatRoom.id = :chatRoomId")
    List<ChatRoomParticipant> findByChatRoomId(@Param("chatRoomId") Long chatRoomId);

    /**
     * 사용자 ID로 참여한 모든 채팅방의 참여자 정보를 조회합니다.
     *
     * @param userId 사용자 ID
     * @return 참여자 정보 목록
     */
    @Query("SELECT p FROM ChatRoomParticipant p WHERE p.user.userId = :userId")
    List<ChatRoomParticipant> findByUserId(@Param("userId") Long userId);

    /**
     * 역할에 따른 참여자 정보를 조회합니다.
     *
     * @param role 역할 (BUYER 또는 SELLER)
     * @return 참여자 정보 목록
     */
    List<ChatRoomParticipant> findByRole(MarketRole role);
}