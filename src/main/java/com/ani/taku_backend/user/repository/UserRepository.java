package com.ani.taku_backend.user.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ani.taku_backend.user.model.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

  // 도메스틱ID와 상태로 유저 조회 -> 이메일과 상태로 유저 조회로 변경
  Optional<User> findByEmailAndStatus(String email, String status);

  // 닉네임 조회
  List<User> findByNickname(String nickname);

  // 유저ID와 상태로 유저 조회
  Optional<User> findByUserIdAndStatus(Long userId, String status);

  // 유저 상태 업데이트
  @Modifying
  @Query("UPDATE User u SET u.status = :status WHERE u.userId = :userId")
  int updateUserStatus(@Param("userId") Long userId, @Param("status") String status);

  // 이메일로 유저 조회
  Optional<User> findByEmail(String email);

  // 유저ID 리스트로 유저 조회
  List<User> findByUserIdIn(List<Long> userIds);


}
