package com.ani.taku_backend.jangter.model.dto.responseDto;

import com.ani.taku_backend.jangter.model.enums.ProductStatus;
import com.mongodb.lang.Nullable;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class ProductFindListResponseDTO {

    private Long id; //상품 아이디

    @Schema(description = "장터글 제목")
    private String title;

    @Schema(description = "장터글 가격")
    private BigDecimal price;


    @Nullable
    @Schema(description = "대표 이미지")
    private String imageUrl;

    @Schema(description = "올린이 유저 이름")
    private String userNickname;
    private Long viewCount;

    @Enumerated(EnumType.STRING)
    private ProductStatus status;  // 상품 상태 (FOR_SALE, RESERVED, SOLD_OUT)


    public ProductFindListResponseDTO(Long id, String title, BigDecimal price, String imageUrl, String userNickname, Long viewCount,  ProductStatus status) {
        this.id = id;
        this.title = title;
        this.price = price;
        this.imageUrl = imageUrl != null ? imageUrl : "defaultImageUrl";
        this.userNickname = userNickname;
        this.viewCount = viewCount;
        this.status = status;
    }


}
