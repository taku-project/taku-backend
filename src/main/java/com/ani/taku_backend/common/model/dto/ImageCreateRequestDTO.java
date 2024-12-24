package com.ani.taku_backend.common.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImageCreateRequestDTO {

    @Schema(description = "이미지 주소")
    private String imageUrl;

    @Schema(description = "UUID로 변환된 파일명")
    private String fileName;

    @Schema(description = "업로드된 파일명")
    private String originalFileName;

    @Schema(description = "파일 타입")
    private String fileType;

    @Max(value = 10485760, message = "파일 크기는 10MB 이하여야 합니다")
    @Schema(description = "파일용량")
    private Integer fileSize;

}
