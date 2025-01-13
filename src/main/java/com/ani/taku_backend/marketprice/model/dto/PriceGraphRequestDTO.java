package com.ani.taku_backend.marketprice.model.dto;

import com.ani.taku_backend.marketprice.model.constant.GraphDisplayOption;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "시세 그래프 요청 DTO")
public class PriceGraphRequestDTO {

    @Schema(description = "검색 키워드", example = "원피스 루피 피규어")
    @NotBlank(message = "키워드는 필수입니다")
    private String keyword;

    @Schema(description = "조회 시작일", example = "2024-01-01")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate fromDate;

    @Schema(description = "조회 종료일", example = "2024-01-31")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate toDate;

    @Schema(description = "그래프 표시 옵션", example = "ALL")
    private GraphDisplayOption displayOption;

    @Override
    public String toString() {
        return "PriceGraphRequestDTO{" +
                "keyword='" + keyword + '\'' +
                ", fromDate=" + fromDate +
                ", toDate=" + toDate +
                ", displayOption=" + displayOption +
                '}';
    }
}