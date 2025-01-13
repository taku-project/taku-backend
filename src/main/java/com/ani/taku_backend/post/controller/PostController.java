package com.ani.taku_backend.post.controller;

import com.ani.taku_backend.common.annotation.ViewCountChecker;
import com.ani.taku_backend.common.annotation.RequireUser;
import com.ani.taku_backend.common.response.CommonResponse;
import com.ani.taku_backend.common.service.ImageService;
import com.ani.taku_backend.post.model.dto.*;
import com.ani.taku_backend.post.service.PostReadService;
import com.ani.taku_backend.post.service.PostService;
import com.ani.taku_backend.user.model.dto.PrincipalUser;
import com.ani.taku_backend.user.model.entity.User;
import com.ani.taku_backend.user.service.BlackUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.MediaType;
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
    private final BlackUserService blackUserService;

    @Operation(summary = "커뮤니티글 전체 조회", description = "검색어와 정렬필터 기능이 포함된 게시글 조회")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "게시글 조회 성공")})
    @GetMapping
    public CommonResponse<PostListResponseDTO> findAllPostList(@ParameterObject @Valid PostListRequestDTO postListRequestDTO) {

        log.debug("postListRequestDTO: {}", postListRequestDTO.getSortFilterType());

        PostListResponseDTO findResultList = postService.findAllPostList(postListRequestDTO);
        return CommonResponse.ok(findResultList);
    }

    @Operation(summary = "커뮤니티 게시글 생성", description = "커뮤티니 게시글을 생성하는 기능")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "게시글 생성 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 접근"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 카테고리")
    })
    @RequireUser
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public CommonResponse<Long> createPost(@Valid PostCreateRequestDTO requestDTO,
                                           @Parameter(hidden = true) PrincipalUser principalUser) {

        User user = blackUserService.checkBlackUser(principalUser); // 유저 검증

        Long createPostId = postService.createPost(requestDTO, user);
        return CommonResponse.created(createPostId);
    }

    @Operation(summary = "커뮤니티 게시글 상세 조회", description = "댓글 미개발")
    @GetMapping("/{postId}")
    public CommonResponse<PostDetailResponseDTO> findPostDetail(
            @Parameter(description = "게시글 ID", required = true) @PathVariable("postId") Long postId,
            @Parameter(description = "조회를 했는지 여부", required = true) @ViewCountChecker Boolean canAddView,
            @Parameter(description = "유저 정보?? 윤정님 확인 필요", required = true) PrincipalUser principalUser
    ) {
        Long currentUserId = null;
        if (principalUser != null) {
            currentUserId = principalUser.getUserId();
        }

        PostDetailResponseDTO detail = postReadService.getPostDetail(postId, canAddView, currentUserId);
        return CommonResponse.ok(detail);
    }

    @Operation(summary = "커뮤니티 게시글 수정", description = "게시글 수정, 기존 이미지를 삭제하거나 추가할 수 있음")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "게시글 수정 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 접근"),
            @ApiResponse(responseCode = "403", description = "존재하지 않는 게시글"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 카테고리")
    })
    @RequireUser
    @PutMapping(path ="/{postId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public CommonResponse<Long> updatePost(
            @Parameter(description = "게시글 ID", required = true, example = "20") @PathVariable("postId") Long postId,
            @Valid PostUpdateRequestDTO requestDTO,
            @Parameter(hidden = true) PrincipalUser principalUser) {

        User user = blackUserService.checkBlackUser(principalUser);             // 유저 검증
        Long updatePostId = postService.updatePost(postId, requestDTO, user);
        return CommonResponse.ok(updatePostId);
    }

    @Operation(
            summary = "커뮤니티 게시글 삭제",
            description = "커뮤니티 게시글 삭제")
    @ApiResponses({
            @ApiResponse(responseCode = "200",description = "게시글 삭제 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 접근"),
            @ApiResponse(responseCode = "403", description = "존재하지 않는 게시글"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 카테고리")
    })
    @RequireUser
    @DeleteMapping("/{postId}")
    public CommonResponse<Long> deletePost(
            @Parameter(description = "게시글 ID", required = true) @PathVariable("postId") Long postId,
            @Parameter(description = "카테고리 ID", required = true) @RequestParam("categoryId") long categoryId) {
        postService.deletePost(postId, categoryId, null);
        return CommonResponse.ok(null);
    }
}
