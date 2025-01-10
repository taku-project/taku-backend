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

/*
@Getter
@Setter
@Builder
@Schema(description = "시세 그래프 조회 요청 DTO")
@AllArgsConstructor
public class PriceGraphRequestDTO {
    @NotBlank(message = "검색 키워드는 필수입니다.")
    @Schema(description = "검색 키워드", example = "원피스 루피 피규어")
    private final String keyword;

    @Schema(description = "조회 시작일", example = "2024-01-01")
    private final LocalDate fromDate;

    @Schema(description = "조회 종료일", example = "2024-03-31")
    private final LocalDate toDate;

    @Schema(description = "그래프 표시 옵션")
    private final GraphDisplayOption displayOption;
}*/
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceGraphRequestDTO {

    @NotBlank(message = "키워드는 필수입니다")
    private String keyword;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate fromDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate toDate;

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
