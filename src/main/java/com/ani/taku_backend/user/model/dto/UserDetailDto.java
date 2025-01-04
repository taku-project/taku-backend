package com.ani.taku_backend.user.model.dto;

import lombok.*;


@Getter
@Builder
public class UserDetailDto {

    private String nickname;

    private String profileImg;

    private String gender;

    private String ageRange;

}
