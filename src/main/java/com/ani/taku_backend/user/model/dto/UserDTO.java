package com.ani.taku_backend.user.model.dto;

import com.ani.taku_backend.user.model.entity.User;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserDTO {

    private Long userId;

    private String nickname;              // 닉네임

    private String providerType;          // 소셜로그인 타입 (예: KAKAO, NAVER 등)

    private String profileImg;            // 프로필이미지 URL

    private String status;                // 유저 상태 (예: ACTIVE, INACTIVE)

    private String domesticId;            // 도메스틱ID

    private String gender;                // 성별

    private String ageRange;              // 연령대

    private String role;                  // 사용자 역할 (예: USER, ADMIN)

    private String email;                 // email


    public static UserDTO of(User user) {
        return UserDTO.builder()
            .userId(user.getUserId())
            .nickname(user.getNickname())
            .providerType(user.getProviderType())
            .profileImg(user.getProfileImg())
            .status(user.getStatus())
            .domesticId(user.getDomesticId())
            .gender(user.getGender())
            .ageRange(user.getAgeRange())
            .role(user.getRole().name())
            .email(user.getEmail())
            .build();
    }
}
