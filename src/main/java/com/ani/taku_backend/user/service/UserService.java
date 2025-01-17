package com.ani.taku_backend.user.service;

import java.util.List;
import java.util.Optional;

import com.ani.taku_backend.user.model.entity.UserStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ani.taku_backend.common.enums.StatusType;
import com.ani.taku_backend.common.enums.UserRole;
import com.ani.taku_backend.user.model.dto.OAuthUserInfo;
import com.ani.taku_backend.user.model.entity.User;
import com.ani.taku_backend.user.repository.UserRepository;
import com.ani.taku_backend.user.model.dto.*;

import static com.ani.taku_backend.user.converter.UserConverter.*;

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
      .email(userInfo.getEmail())
      .domesticId(userInfo.getDomesticId())
      .status(UserStatus.ACTIVE)
      .nickname(userInfo.getNickname())
      .profileImg(userInfo.getImageUrl())
      .providerType(userInfo.getProviderType().toString())
      .gender(userInfo.getGender())
      .ageRange(userInfo.getAgeRange())
      .role(UserRole.USER)
      .build();

    User savedUser = userRepository.save(user);
    log.info("savedUser : {}", savedUser);

    return savedUser;
  }

  // 유저 조회
  public Optional<User>  getUser(String email) {
    Optional<User> byDomesticId = this.userRepository.findByEmailAndStatus(email, StatusType.ACTIVE.name());
    return byDomesticId;
  }

  // 닉네임 체크
  public boolean checkNickname(String nickname) {
    List<User> users = this.userRepository.findByNickname(nickname);
    return users.isEmpty() ? false : true;
  }

  // 유저 삭제
  public Optional<User> findByUserIdAndStatus(Long userId, StatusType status) {
    return this.userRepository.findByUserIdAndStatus(userId, status.name());
  }

  // 유저 상태 업데이트
  @Transactional //transactional 붙여주지 않으면 오류 난다.
  public int updateUserStatus(Long userId, StatusType status) {
    return this.userRepository.updateUserStatus(userId, status.name());
  }

  public UserDetailDto getUserDetail(Long userId){

    //Optional로 해야하는 이유
    Optional<User> user = userRepository.findById(userId);

    return toUserDetailDto(user.get().getNickname(), user.get().getGender(), user.get().getAgeRange(), user.get().getProfileImg());

  }


  @Transactional
  public void updateNickname(Long userId, String nickname){

    userRepository.updateNickname(userId, nickname);

  }

  @Transactional
  public void updateProfileImg(Long userId, String profileImg){
    userRepository.updateProfileImg(userId, profileImg);
  }

}

