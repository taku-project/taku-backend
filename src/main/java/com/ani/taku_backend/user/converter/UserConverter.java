package com.ani.taku_backend.user.converter;

import com.ani.taku_backend.user.model.dto.*;



public class UserConverter {

    public static UserDetailDto toUserDetailDto(String nickname, String gender, String ageRange, String profileImg){

        return UserDetailDto.builder()
                .nickname(nickname)
                .gender(gender)
                .ageRange(ageRange)
                .profileImg(profileImg)
                .build();

    }
}
