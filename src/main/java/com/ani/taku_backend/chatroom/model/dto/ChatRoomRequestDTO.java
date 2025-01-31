package com.ani.taku_backend.chatroom.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

public record ChatRoomRequestDTO(
        @NotNull(message = "상품 ID는 필수입니다.")
        Long articleId,

        @NotNull(message = "구매자 ID는 필수입니다.")
        Long buyerId,

        @NotNull(message = "판매자 ID는 필수입니다.")
        Long sellerId
) {
    @Builder
    public ChatRoomRequestDTO {
    }
}