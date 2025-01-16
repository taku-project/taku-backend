package com.ani.taku_backend.user.model.dto.requestDto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserEditDto {

    @Schema(description = "업로드할 닉네임", nullable = true)
    private String nickname;

    @Schema(description = "업로드된 파일명", nullable = true)
    private String originalFileName;

    @Schema(description = "파일 타입", nullable = true)
    private String fileType;

    @Max(value = 10485760, message = "파일 크기는 10MB 이하여야 합니다")
    @Schema(description = "파일용량", nullable = true)
    private Integer fileSize;

}
