package com.ani.taku_backend.chatroom.domain.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "채팅방 생성 요청 DTO")
public record ChatRoomRequestDTO(
        @Schema(description = "상품 ID", example = "1")
        @NotNull(message = "상품 ID는 필수입니다.")
        Long articleId,

        @Schema(description = "구매자 ID", example = "58")
        @NotNull(message = "구매자 ID는 필수입니다.")
        Long buyerId
) {
}