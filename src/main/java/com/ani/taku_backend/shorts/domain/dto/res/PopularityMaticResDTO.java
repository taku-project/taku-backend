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

    public PopularityMaticResDTO(Shorts.PopularityMatic popularityMatic) {
        if(popularityMatic != null) {
            this.views = popularityMatic.getViews();
            this.commentsCount = popularityMatic.getCommentsCount();
            this.likes = popularityMatic.getLikes();
            this.dislikes = popularityMatic.getDislikes();
        }
    }
}
