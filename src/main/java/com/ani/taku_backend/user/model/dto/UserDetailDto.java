package com.ani.taku_backend.user.model.dto;

import lombok.Builder;
import lombok.Getter;


@Getter
@Builder
public class UserDetailDto {

    private String nickname;

    private String profileImg;

    private String gender;

    private String ageRange;

}
