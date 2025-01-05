package com.ani.taku_backend.post.controller;

import com.ani.taku_backend.common.annotation.ViewCountChecker;
import com.ani.taku_backend.common.annotation.RequireUser;
import com.ani.taku_backend.common.response.CommonResponse;
import com.ani.taku_backend.post.model.dto.*;
import com.ani.taku_backend.post.service.PostReadService;
import com.ani.taku_backend.post.service.PostService;
import com.ani.taku_backend.user.model.dto.PrincipalUser;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/community/posts")
public class PostController {

    private final PostService postService;
    private final PostReadService postReadService;

    @Operation(summary = "커뮤니티 게시글 조회(정렬, 검색)",
            description = "")
    @GetMapping
    public CommonResponse<List<PostListResponseDTO>> findAllPost(PostListRequestDTO postListRequestDTO) {
        List<PostListResponseDTO> postList = postService.findAllPost(postListRequestDTO);
        return CommonResponse.ok(postList);
    }

    @Operation(summary = "커뮤니티 게시글 생성",
            description = """
            """)
    @PostMapping
    public CommonResponse<Long> createPost(
            @Valid @RequestPart("createPost") PostCreateRequestDTO requestDTO,
            @RequestPart(value = "postImage", required = false) List<MultipartFile> imageList) {

        Long createPostId = postService.createPost(requestDTO, imageList, null);
        return CommonResponse.created(createPostId);
    }

    @Operation(summary = "커뮤니티 게시글 상세 조회", description = """
            1. 게시글 상세 조회(댓글은 나중에 작업)
            """)
    @GetMapping("/{postId}")
    public CommonResponse<PostDetailResponseDTO> findPostDetail(
            @PathVariable Long postId,
            @ViewCountChecker Boolean canAddView,
            PrincipalUser principalUser
    ) {
        Long currentUserId = null;
        if (principalUser != null) {
            currentUserId = principalUser.getUserId();
        }

        PostDetailResponseDTO detail = postReadService.getPostDetail(postId, canAddView, currentUserId);
        return CommonResponse.ok(detail);
    }

    @Operation(summary = "커뮤니티 게시글 수정",
            description = """
            """)
    @RequireUser
    @PutMapping("/{postId}")
    public CommonResponse<Long> updatePost(
            @PathVariable Long postId,
            @Valid @RequestPart("updatePost") PostUpdateRequestDTO requestDTO,
            @RequestPart(value = "updatePostImage", required = false) List<MultipartFile> imageList) {

        Long updatePostId = postService.updatePost(requestDTO, postId, imageList, null);
        return CommonResponse.ok(updatePostId);
    }

    @Operation(summary = "커뮤니티 게시글 삭제")
    @RequireUser
    @DeleteMapping("/{postId}")
    public CommonResponse<Long> deletePost(
            @PathVariable Long postId, long categoryId) {
        postService.deletePost(postId, categoryId, null);
        return CommonResponse.ok(null);
    }
}
