package com.ani.taku_backend.post.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostCreateRequestDTO {

    @Schema(description = "카테고리Id(필수값, 테스트시 69나 70으로 입력)", example = "69")
    @NotNull
    private Long categoryId;

    @Schema(description = "제목(필수값)", example = "편하게 테스트 제목!")
    @Size(max = 150, message = "제목은 최대 150글자까지 입력 가능합니다.")
    @NotNull
    private String title;

    @Schema(description = "내용(필수값)", example = "편하게 테스트 내용!")
    @Size(max = 3000, message = "내용은 최대 3000글자까지 입력 가능합니다.")
    @NotNull
    private String content;

    @Schema(description = "게시글 첨부 이미지 파일 (여러 파일 업로드 가능)")
    private List<MultipartFile> imageList = new ArrayList<>();
}
