package com.ani.taku_backend.user.service;

import com.ani.taku_backend.common.enums.UserRole;
import com.ani.taku_backend.user.model.dto.OAuthUserInfo;
import com.ani.taku_backend.user.model.dto.UserDetailDto;
import com.ani.taku_backend.user.model.entity.User;
import com.ani.taku_backend.user.model.entity.UserStatus;
import com.ani.taku_backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import com.ani.taku_backend.common.model.entity.Image;
import com.ani.taku_backend.common.repository.ImageRepository;
import com.ani.taku_backend.common.service.FileService;
import com.ani.taku_backend.user.model.entity.UserImage;
import com.ani.taku_backend.user.repository.UserImageRepository;



import static com.ani.taku_backend.user.converter.UserConverter.toUserDetailDto;


@Service
@RequiredArgsConstructor
@Log4j2
public class UserService {

  private final UserRepository userRepository;

  private final UserImageRepository userImageRepository;

  private final ImageRepository imageRepository;

  private final FileService fileService;

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
  public Optional<User> getUserByDomesticId(String domesticId) {
      return this.userRepository.findByDomesticIdAndStatus(domesticId, UserStatus.ACTIVE);
  }

  // 닉네임 체크
  public boolean isNicknameDuplication(String nickname) {
    List<User> users = this.userRepository.findByNickname(nickname);
    return !users.isEmpty();
  }

  public Optional<User> findByUserIdAndStatus(Long userId, UserStatus status) {
    return this.userRepository.findByUserIdAndStatus(userId, status);
  }

  // 유저 상태 업데이트
  @Transactional //transactional 붙여주지 않으면 오류 난다.
  public int updateUserStatus(Long userId, UserStatus status) {
    return this.userRepository.updateUserStatus(userId, status);
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
  public void updateProfileImg(Long userId, String profileImg, Integer fileSize, String fileType, String originalName, String imageUrl){


    //기존 image soft delete
    Optional<UserImage> userImage = userImageRepository.findByUser_UserId(userId);

    if(userImage.isPresent()) { //만약, userImage Repo에 image가 있다면,
      Long imageId = userImage.get().getImage().getId();

      imageRepository.softDeleteByImageId(imageId);

      //userImage Repository에서 지우기
      userImageRepository.deleteByUser_UserId(userId);

      //cloudflare r2에서 지우기
      fileService.deleteImageFile(userImage.get().getImage().getFileName());


    }


    //새로운 iamge 넣기
    Optional<User> user = userRepository.findById(userId);

    String fileName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);

    Image image = Image.builder()
            .imageUrl(imageUrl)
            .fileSize(fileSize)
            .fileName(fileName)
            .fileType(fileType)
            .originalName(originalName)
            .user(user.get())
            .build();

    imageRepository.save(image);

    UserImage userImage1 = UserImage.builder().user(user.get()).image(image).build();

    userImageRepository.save(userImage1);

    userRepository.updateProfileImg(userId, profileImg);


  }

}

