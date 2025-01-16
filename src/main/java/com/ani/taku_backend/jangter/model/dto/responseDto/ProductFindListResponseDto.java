package com.ani.taku_backend.jangter.model.dto.responseDto;

import com.mongodb.lang.Nullable;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class ProductFindListResponseDto {

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

    public ProductFindListResponseDto(Long id, String title, BigDecimal price, String imageUrl, String userNickname, Long viewCount) {
        this.id = id;
        this.title = title;
        this.price = price;
        this.imageUrl = imageUrl != null ? imageUrl : "defaultImageUrl";
        this.userNickname = userNickname;
        this.viewCount = viewCount;
    }
}
