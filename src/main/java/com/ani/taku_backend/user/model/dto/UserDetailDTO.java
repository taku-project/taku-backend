package com.ani.taku_backend.user.model.dto;


import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserDetailDTO {

    private String nickname;

    private String profileImg;

    private String gender;

    private String ageRange;

}
