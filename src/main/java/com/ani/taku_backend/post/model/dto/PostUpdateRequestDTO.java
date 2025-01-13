package com.ani.taku_backend.post.model.dto;

import com.ani.taku_backend.common.model.dto.ImageCreateRequestDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
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
public class PostUpdateRequestDTO {

    @Schema(description = "카테고리Id(필수값, 테스트시 69나 70으로 입력)", example = "69")
    @NotNull
    private Long categoryId;

    @Schema(description = "제목(필수값)", example = "편하게 테스트 제목 수정!")
    @Size(max = 150, message = "제목은 최대 150글자까지 입력 가능합니다.")
    @NotNull
    private String title;

    @Schema(description = "내용(필수값)", example = "편하게 테스트 내용 수정!")
    @Size(max = 3000, message = "내용은 최대 3000글자까지 입력 가능합니다.")
    @NotNull
    private String content;

    @Schema(description = "삭제할 이미지 URL 리스트, DB에서 이미지 URL를 조회해야하므로 번거로울 시 Send empty value 체크 해제 후 테스트 진행")
    private List<String> deleteImageUrl;

    @Schema(description = "업데이트할 이미지파일 (여러 파일 업로드 가능), 이미지 파일 업로드 안할 시 Send empty value 체크 해제 후 테스트 진행")
    private List<MultipartFile> imageList = new ArrayList<>();
}
