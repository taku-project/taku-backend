package com.ani.taku_backend.shorts.domain.dto.res;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ShortsResponseDTO {
    private String shortsId;
    private String m3u8Url;
    private String userProfileImg;
    private String description;
    private boolean userLike;
    private boolean userDislike;
    private PopularityMaticResDTO popularityMatic;
}
