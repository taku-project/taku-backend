package com.ani.taku_backend.marketprice.model.dto;

import com.ani.taku_backend.marketprice.model.constant.GraphDisplayOption;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
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
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
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
