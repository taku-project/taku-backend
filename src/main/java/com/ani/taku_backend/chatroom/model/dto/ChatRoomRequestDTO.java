package com.ani.taku_backend.chatroom.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoomRequestDTO {

    @NotNull(message = "상품 ID는 필수입니다.")
    private Long articleId;

    @NotNull(message = "구매자 ID는 필수입니다.")
    private Long buyerId;

    @NotNull(message = "판매자 ID는 필수입니다.")
    private Long sellerId;

    @Builder
    public ChatRoomRequestDTO(Long articleId, Long buyerId, Long sellerId) {
        this.articleId = articleId;
        this.buyerId = buyerId;
        this.sellerId = sellerId;
    }
}