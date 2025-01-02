package com.ani.taku_backend.shorts.domain.dto.res;

import com.ani.taku_backend.shorts.domain.entity.Shorts;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class PopularityMaticResDTO {
    private int views;
    private int commentsCount;
    private int likes;
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
