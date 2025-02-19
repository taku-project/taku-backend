package com.ani.taku_backend.category_bookmark.dto.res;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "카테고리 북마크 반환 객체")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class CategoryBookmarkDTO {
    @Schema(description = "카테고리 북마크 ID")
    private Long bookmarkId;
    @Schema(description = "카테고리 이름")
    private String categoryName;
    @Schema(description = "카테고리 image URL")
    private String categoryImageUrl;
}
