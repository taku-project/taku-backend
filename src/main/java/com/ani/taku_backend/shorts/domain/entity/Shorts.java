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
    private String title;
    private Long userId;
    private String nickname;
    private String profileImg;
    private String role;
    private String description;
    private List<String> tags;
    private VideoMetadata fileInfo;
    private PopularityMatic popularityMatics;

    @CreatedDate
    private LocalDateTime createdAt;

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
    public static class PopularityMatic {
        private int views;
        private int commentsCount;
        private int likes;
        private int dislikes;

        public static PopularityMatic create() {
            return new PopularityMatic(0, 0, 0, 0);
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
                .popularityMatics(PopularityMatic.create())
                .build();
    }
}
