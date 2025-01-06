package com.ani.taku_backend.shorts.domain.dto.res;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "Shorts 정보에 대한 응답 DTO")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ShortsResponseDTO {
    @JsonProperty("shorts_id")
    @Schema(description = "Shorts의 고유 ID", example = "6775805e1a08421aac32df42")
    private String shortsId;

    @JsonProperty("m3u8_url")
    @Schema(description = "M3U8 파일 URL", example = "https://example.com/video.m3u8")
    private String m3u8Url;

    @JsonProperty("profile_img_url")
    @Schema(description = "사용자의 프로필 이미지 URL", example = "https://example.com/user/profile.jpg")
    private String profileImgUrl;

    @JsonProperty("description")
    @Schema(description = "쇼츠 대한 한 줄 설명", example = "이 동영상은 애니메이션에 관한 내용입니다.")
    private String description;

    @JsonProperty("user_like_interaction")
    @Schema(description = "사용자의 상호작용 정보 - 좋아요, 싫어요 눌렀는지 여부")
    private ShortsLikeInteractionResponse userLikeInteraction;

    @JsonProperty("popularity_matic")
    @Schema(description = "Shorts의 인기 지표 정보")
    private PopularityMaticResDTO popularityMatic;
}
