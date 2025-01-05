package com.ani.taku_backend.shorts.domain.entity;

import com.ani.taku_backend.shorts.domain.dto.ShortsCreateReqDTO;
import com.ani.taku_backend.shorts.domain.dto.ShortsFFmPegUrlResDTO;
import com.ani.taku_backend.user.model.entity.User;
import jakarta.persistence.Enumerated;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "shorts")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Shorts {
    @Id
    private String id;
    @Field(name = "title")
    private String title;
    @Field(name = "user_id")
    private Long userId;
    @Field(name = "nickname")
    private String nickname;
    @Field(name = "profile_img")
    private String profileImg;
    @Field(name = "role")
    private String role;
    @Field(name = "description")
    private String description;
    @Field(name = "tags")
    private List<String> tags;
    @Field(name = "file_info")
    private VideoMetadata fileInfo;
    @Field(name = "popularity_metrics")
    private PopularityMetric popularityMetrics;

    @CreatedDate
    private LocalDateTime createdAt;

    public void addLike(boolean hasDislike) {
        if(hasDislike) {
            int dislikes = this.popularityMetrics.dislikes;
            if(dislikes > 0) {
                this.popularityMetrics.dislikes -= 1;
            }
        }
        this.popularityMetrics.likes += 1;
    }

    @Getter
    @Builder
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    public static class VideoMetadata {
        private String originFileName;
        private String originFileRemotePath;
        private int duration;
        private List<String> remoteStorageUrl;
        private int fileSize;
        @Enumerated
        private VideoType fileType;

        public static VideoMetadata create(String originFileName, String originFileRemotePath, int fileSize, VideoType fileType, ShortsFFmPegUrlResDTO fmPegUrlInfoDTO) {
            List<String> remoteStorageUrl = new ArrayList<>(fmPegUrlInfoDTO.getSegments());
            remoteStorageUrl.add(fmPegUrlInfoDTO.getM3u8Url());

            return VideoMetadata.builder()
                    .originFileName(originFileName)
                    .originFileRemotePath(originFileRemotePath)
                    .remoteStorageUrl(remoteStorageUrl)
                    .duration((int) fmPegUrlInfoDTO.getDuration())
                    .fileSize(fileSize)
                    .fileType(fileType)
                    .build();
        }
    }

    @Getter
    @Builder
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    public static class PopularityMetric {
        private int views;
        private int commentsCount;
        private int likes;
        private int dislikes;

        public static PopularityMetric create() {
            return new PopularityMetric(0, 0, 0, 0);
        }
    }

    public static Shorts create(User user, ShortsCreateReqDTO createReqDTO, String filePath, ShortsFFmPegUrlResDTO ffmpegUrlDTO) {
        MultipartFile file = createReqDTO.getFile();
        String contentType = file.getContentType();
        VideoType videoType = VideoType.fromExtension(contentType);

        return Shorts.builder()
                .userId(user.getUserId())
                .nickname(user.getNickname())
                .profileImg(user.getProfileImg())
                .role(user.getRole())
                .title(createReqDTO.getTitle())
                .fileInfo(
                    VideoMetadata.create(file.getOriginalFilename(), filePath, (int) file.getSize(), videoType, ffmpegUrlDTO)
                )
                .description(createReqDTO.getDescription())
                .tags(createReqDTO.getTags())
                .popularityMetrics(PopularityMetric.create())
                .build();
    }
}
