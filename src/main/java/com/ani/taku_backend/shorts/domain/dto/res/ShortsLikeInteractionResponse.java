package com.ani.taku_backend.shorts.domain.dto.res;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "사용자의 상호작용 정보")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ShortsLikeInteractionResponse {
    @Schema(description = "사용자가 좋아요를 눌렀는지 여부", example = "true")
    private boolean userLike;
    @Schema(description = "사용자가 싫어요를 눌렀는지 여부", example = "false")
    private boolean userDislike;
}
