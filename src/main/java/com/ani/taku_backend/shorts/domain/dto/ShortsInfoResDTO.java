package com.ani.taku_backend.shorts.domain.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.ani.taku_backend.shorts.domain.entity.Shorts;
import com.ani.taku_backend.shorts.domain.entity.VideoType;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Builder
@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Slf4j
public class ShortsInfoResDTO {

    private String id;
    private String title;
    private String description;
    private List<String> tags;
    @JsonProperty("file_info")
    private FileInfoDTO fileInfo;
    @JsonProperty("popularity_matic")
    private PopularityMaticDTO popularityMatic;
    @JsonProperty("created_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;


    public static ShortsInfoResDTO of(Shorts shorts) {
        return ShortsInfoResDTO.builder()
                .id(shorts.getId())
                .title(shorts.getTitle())
                .description(shorts.getDescription())
                .tags(shorts.getTags())
                .fileInfo(getFileInfo(shorts))
                .popularityMatic(getPopularityMatic(shorts))
                .createdAt(shorts.getCreatedAt())
                .build();
    }

    /**
     * 파일 정보를 조회하여 파일 정보를 반환한다.
     * @param shorts
     * @return
     */
    private static FileInfoDTO getFileInfo(Shorts shorts) {
        List<String> remoteStorageUrl = shorts.getFileInfo().getRemoteStorageUrl();

        if(remoteStorageUrl.size() == 0) {
            return null;
        }

        // ts 파일을 제외한 m3u8 파일을 찾는다.
        String playUrl = remoteStorageUrl.stream().filter(url -> url.toLowerCase().contains(".m3u8")).findFirst().orElse(null);
        return FileInfoDTO.builder()
                .playUrl(playUrl)
                .duration(shorts.getFileInfo().getDuration())
                .build();

    }

    /**
     * 쇼츠 정보를 변환하여 반환한다.
     * @param shorts
     * @return
     */
    private static PopularityMaticDTO getPopularityMatic(Shorts shorts) {
        return PopularityMaticDTO.builder()
                .views(shorts.getPopularityMatics().getViews())
                .likes(shorts.getPopularityMatics().getLikes())
                .comments(shorts.getPopularityMatics().getCommentsCount())
                .dislikes(shorts.getPopularityMatics().getDislikes())
                .build();
    }

    @Builder
    @Data
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    private static class FileInfoDTO {
        @JsonProperty("play_url")
        private String playUrl;
        private int duration;

    }

    @Builder
    @Data
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    private static class PopularityMaticDTO {
        private int views;
        private int likes;
        private int comments;
        private int dislikes;
    }

}