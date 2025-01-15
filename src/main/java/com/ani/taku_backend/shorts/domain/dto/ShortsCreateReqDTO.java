package com.ani.taku_backend.shorts.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Schema(description = "Shorts 생성 요청 객체")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ShortsCreateReqDTO {

    @Schema(description = "쇼츠 파일", required = true)
    @NotNull
    private MultipartFile file;
    @Schema(description = "제목", required = true)
    @NotBlank
    private String title;

    @Schema(description = "한 줄 설명", required = true)
    @NotBlank
    private String description;

    @Schema(description = "해시태그 목록", required = true)
    private List<String> tags;
}
