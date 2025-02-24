package com.ani.taku_backend.category_bookmark.controller;

import com.ani.taku_backend.category_bookmark.dto.res.CategoryBookmarkReqDTO;
import com.ani.taku_backend.category_bookmark.service.CategoryBookmarkService;
import com.ani.taku_backend.common.response.CommonResponse;
import com.ani.taku_backend.user.model.dto.PrincipalUser;
import com.ani.taku_backend.user.model.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "카테고리 북마크 API", description = "카테고리 북마크 CRUD API")
@RestController
@RequestMapping("/api/category-bookmark")
@RequiredArgsConstructor
public class ApiCategoryBookmarkController {
    private final CategoryBookmarkService categoryBookmarkService;

    @Operation(summary = "카테고리 상세 북마크 추가", description = "카테고리 상세에서 사용자가 북마크를 합니다.")
    @Parameter(name = "id", description = "카테고리 ID", required = true, in = ParameterIn.PATH, schema = @Schema(type = "integer", format = "int64"))
    @PostMapping("/{id}")
    public CommonResponse<Void> createCategoryBookmark(@AuthenticationPrincipal PrincipalUser userDetails,
        @PathVariable("id") Long categoryId) {
        User user = userDetails.getUser();
        categoryBookmarkService.createCategoryBookmark(user, categoryId);
        return CommonResponse.ok(null);
    }

    @Operation(summary = "카테고리 상세 북마크 삭제", description = "카테고리 상세에서 사용자가 북마크를 취소 합니다.")
    @Parameter(name = "id", description = "카테고리 ID", required = true, in = ParameterIn.PATH, schema = @Schema(type = "integer", format = "int64"))
    @DeleteMapping("/{id}")
    public CommonResponse<Void> deleteCategoryBookmark(@AuthenticationPrincipal PrincipalUser userDetails,
        @PathVariable("id") Long categoryBookmarkId) {
        User user = userDetails.getUser();
        categoryBookmarkService.deleteCategoryBookmark(user, categoryBookmarkId);
        return CommonResponse.ok(null);
    }

    @Operation(summary = "카테고리 북마크 목록", description = "사용자가 북마크한 목록을 보여줍니다.")
    @GetMapping
    public CommonResponse<CategoryBookmarkReqDTO> getCategoryBookmark(@AuthenticationPrincipal PrincipalUser userDetails) {
        User user = userDetails.getUser();

        CategoryBookmarkReqDTO result = categoryBookmarkService.findCategoryBookmark(user);
        return CommonResponse.ok(result);
    }
}
