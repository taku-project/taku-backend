package com.ani.taku_backend.user.model.dto.requestDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileImgRequestDTO {
    private Long userId;
    private String profileImg;
    private Integer fileSize;
    private String fileType;
    private String originalFileName;

}
