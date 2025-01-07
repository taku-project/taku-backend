package com.ani.taku_backend.shorts.domain.dto.res;

import com.ani.taku_backend.shorts.domain.entity.Shorts;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "Shorts의 인기 지표")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class PopularityMaticResDTO {
    @Schema(description = "조회수", example = "1200")
    private int views;

    @Schema(description = "댓글 수", example = "35")
    private int commentsCount;

    @Schema(description = "좋아요 수", example = "300")
    private int likes;

    @Schema(description = "싫어요 수", example = "15")
    private int dislikes;

    public PopularityMaticResDTO(Shorts.PopularityMetric popularityMetric) {
        if(popularityMetric != null) {
            this.views = popularityMetric.getViews();
            this.commentsCount = popularityMetric.getCommentsCount();
            this.likes = popularityMetric.getLikes();
            this.dislikes = popularityMetric.getDislikes();
        }
    }
}
