package com.ani.taku_backend.user_jangter.dto;

import com.querydsl.core.annotations.QueryProjection;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Schema(description = "유저 구매 목록 응답 객체")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserPurchaseResponseDTO {

    @Schema(description = "거래 완료 ID")
    private Long id;
    @Schema(description = "장터 글 ID")
    private Long jangterId;
    @Schema(description = "제목")
    private String title;
    @Schema(description = "구매가격")
    private BigDecimal price;
    @Schema(description = "상품 카테고리 이름")
    private String categoryName;

    @QueryProjection
    public UserPurchaseResponseDTO(Long id, Long jangterId, String title, BigDecimal price, String categoryName) {
        this.id = id;
        this.jangterId = jangterId;
        this.title = title;
        this.price = price;
        this.categoryName = categoryName;
    }
}
