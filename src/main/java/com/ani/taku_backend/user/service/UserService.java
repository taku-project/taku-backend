package com.ani.taku_backend.user.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ani.taku_backend.common.enums.UserRole;
import com.ani.taku_backend.user.model.dto.OAuthUserInfo;
import com.ani.taku_backend.user.model.entity.User;
import com.ani.taku_backend.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@RequiredArgsConstructor
@Log4j2
public class UserService {

  private final UserRepository userRepository;

  // 유저 등록
  public User registerUser(OAuthUserInfo userInfo) {
    User user = User.builder()
      .domesticId(userInfo.getEmail())
      .status("ACTIVE")
      .nickname(userInfo.getNickname())
      .profileImg(userInfo.getImageUrl())
      .providerType(userInfo.getProviderType().toString())
      .role(UserRole.USER.toString())
      .build();

    User savedUser = userRepository.save(user);
    log.info("savedUser : {}", savedUser);

    return savedUser;
  }

  // 유저 조회
  public Optional<User>  getUser(String domesticId) {
    Optional<User> byDomesticId = this.userRepository.findByDomesticIdAndStatus(domesticId, "ACTIVE");
    return byDomesticId;
  }

  // 닉네임 체크
  public boolean checkNickname(String nickname) {
    return this.userRepository.findByNickname(nickname).isPresent();
  }

  // 유저 삭제
  public Optional<User> findByUserIdAndStatus(Long userId, String status) {
    return this.userRepository.findByUserIdAndStatus(userId, status);
  }

  // 유저 상태 업데이트
  @Transactional
  public int updateUserStatus(Long userId, String status) {
    return this.userRepository.updateUserStatus(userId, status);
  }
}

