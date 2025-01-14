package com.ani.taku_backend.jangter.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@Schema(description = "게시글 업데이트 요청 DTO")
public class ProductUpdateRequestDTO {

    @Schema(description = "카테고리 ID (필수값, 현재 1 ~ 4까지 있음)", example = "1")
    @NotNull(message = "{NotNull.categoryId}")
    private Long categoryId;

    @Schema(description = "판매글 제목 (필수값)", example = "편하게 테스트 제목 수정")
    @NotNull(message = "{NotNull.title}")
    @Size(max = 150, message = "150자 이하로 입력해 주세요.")
    private String title;

    @Schema(description = "판매글 본문", example = "편하게 테스트 본문 수정")
    @NotNull(message = "{NotNull.description}")
    @Size(max = 3000, message = "3000자 이하로 입력해 주세요.")
    private String description;

    @Schema(description = "가격", example = "50000")
    @NotNull(message = "{NotNull.price}")
    @DecimalMin(value = "0.01", message = "가격은 0.01 이상이어야 합니다.")
    @DecimalMax(value = "10000000.00", message = "가격은 10,000,000 이하이어야 합니다.")
    private BigDecimal price;

    @Schema(description = "삭제할 이미지 URL 리스트, DB에서 이미지 URL를 조회해야하므로 번거로울 시 Send empty value 체크 해제 후 테스트 진행")
    private List<String> deleteImageUrl;

    @Schema(description = "업데이트할 이미지파일 (여러 파일 업로드 가능), 이미지 파일 업로드 안할 시 Send empty value 체크 해제 후 테스트 진행")
    private List<MultipartFile> imageList = new ArrayList<>();

}
