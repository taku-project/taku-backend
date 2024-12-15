package com.ani.taku_backend.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ani.taku_backend.user.model.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

  // 도메스틱ID와 상태로 유저 조회 -> 이메일과 상태로 유저 조회로 변경
  Optional<User> findByEmailAndStatus(String email, String status);

  // 이메일로 유저 조회
  Optional<User> findByEmail(String email);
}
