package com.ani.taku_backend.shorts.domain.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ShortsField {
    DOCUMENT("shorts"),
    ID("_id"),
    TITLE("title"),
    USER_ID("user_id"),
    NICKNAME("nickname"),
    PROFILE_IMG("profile_img"),
    ROLE("role"),
    DESCRIPTION("description"),
    TAGS("tags"),
    FILE_INFO("file_info"),
    POPULARITY_METRICS("popularity_metrics"),
    CREATED_AT("created_at");

    private final String fieldName;

    @Getter
    @RequiredArgsConstructor
    public enum FileInfo {
        ORIGIN_FILE_NAME("origin_file_name"),
        ORIGIN_FILE_REMOTE_PATH("origin_file_remote_path"),
        DURATION("duration"),
        REMOTE_STORAGE_URL("remote_storage_url"),
        FILE_SIZE("file_size"),
        FILE_TYPE("file_type");

        private final String subFieldName;

        public String fullFieldName() {
            return ShortsField.FILE_INFO.getFieldName() + "." + subFieldName;
        }
    }

    @Getter
    @RequiredArgsConstructor
    public enum PopularityMetrics {
        VIEWS("views"),
        COMMENTS_COUNT("comments_count"),
        LIKES("likes"),
        DISLIKES("dislikes");

        private final String subFieldName;

        public String fullFieldName() {
            return ShortsField.POPULARITY_METRICS.getFieldName() + "." + subFieldName;
        }
    }
}