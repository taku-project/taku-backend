package com.ani.taku_backend.post.controller;

import com.ani.taku_backend.common.annotation.RequireUser;
import com.ani.taku_backend.common.annotation.ViewCountChecker;
import com.ani.taku_backend.common.response.CommonResponse;
import com.ani.taku_backend.post.model.dto.*;
import com.ani.taku_backend.post.model.enums.PopularPeriodType;
import com.ani.taku_backend.post.service.PostService;
import com.ani.taku_backend.user.model.dto.PrincipalUser;
import com.ani.taku_backend.user.model.entity.User;
import com.ani.taku_backend.user.service.BlackUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;


@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/community/posts")
public class PostController {

    private final PostService postService;
    private final BlackUserService blackUserService;

    @Operation(
        summary = "커뮤니티글 전체 조회",
        description = "검색어와 정렬필터 기능이 포함된 게시글 조회",
        parameters = {
            @Parameter(name = "page", description = "페이지 번호(0부터 시작)", example = "0"),
            @Parameter(name = "size", description = "페이지 수", example = "20"),
            @Parameter(name = "sort",
                    description = """
                        정렬 필터와 정렬 방식,\n
                        입력 방법: 정렬 필터,정렬 방식(띄어쓰기 없어야함)\n
                        정렬 필터: id(최신순), views(조회수순)\n
                        정렬 방식: desc(내림차순, 기본값), asc(오름차순)
                        """,
                    example = "id,desc")
        }
    )
    @ApiResponses({@ApiResponse(responseCode = "200", description = "게시글 조회 성공")})
    @GetMapping

    public CommonResponse<PostListResponseDTO> findPostPage(
            @ParameterObject @Valid PostListRequestDTO postListRequestDTO,
            @Parameter(hidden = true) @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {

        postListRequestDTO.postListRequestValidate();

        PostListResponseDTO findResultList = postService.findPostList(postListRequestDTO, pageable);
        return CommonResponse.ok(findResultList);
    }

    @Operation(summary = "커뮤니티 게시글 생성",
            description = """
                    커뮤티니 게시글을 생성하는 기능(스웨거 오류로 여기다 설명)\n
                    imageList - 추가할 이미지 리스트(이미지 파일)
                    """)
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "게시글 생성 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 접근"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 카테고리")
    })
    @RequireUser
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public CommonResponse<Long> createPost(@Valid PostCreateRequestDTO requestDTO,
                                         @Parameter(hidden = true) PrincipalUser principalUser) {
        User user = blackUserService.checkBlackUser(principalUser);
        Long createPostId = postService.createPost(requestDTO, user);
        return CommonResponse.created(createPostId);
    }

    @Operation(summary = "커뮤니티 게시글 상세 조회", description = "게시글의 상세 정보와 댓글을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "게시글 조회 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 게시글")
    })
    @GetMapping("/{postId}")
    public CommonResponse<PostDetailResponseDTO> findPostDetail(
            @Parameter(description = "게시글 ID") @PathVariable Long postId,
            @Parameter(description = "조회수 증가 여부") @ViewCountChecker Boolean canAddView,
            @Parameter(description = "로그인한 사용자 정보 (없을 경우 null)", hidden = true) 
            @AuthenticationPrincipal PrincipalUser principalUser) {
            
        Long currentUserId = null;
        if (principalUser != null && principalUser.getUser() != null) {
            currentUserId = principalUser.getUser().getUserId();
        }
        
        PostDetailResponseDTO detail = postService.getPostDetail(postId, canAddView, currentUserId);
        return CommonResponse.ok(detail);
    }

    @Operation(summary = "커뮤니티 게시글 수정",
            description = """
                    게시글 수정, 기존 이미지를 삭제하거나 추가할 수 있음(스웨거 오류로 여기다 설명)\n
                    deleteImageUrl - 기존 글에서 삭제된 이미지 Url 리스트(문자열)\n
                    imageList - 추가된 이미지 리스트(이미지 파일)
                    """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "게시글 수정 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 접근"),
            @ApiResponse(responseCode = "403", description = "존재하지 않는 게시글"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 카테고리")
    })
    @RequireUser
    @PutMapping(path = "/{postId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public CommonResponse<Long> updatePost(
            @Parameter(description = "게시글 ID(구글 테스트 토큰을 입력하세요)", required = true, example = "32") @PathVariable("postId") Long postId,
            @Valid PostUpdateRequestDTO requestDTO,
            @Parameter(hidden = true) PrincipalUser principalUser) {
        User user = blackUserService.checkBlackUser(principalUser);
        Long updatePostId = postService.updatePost(postId, requestDTO, user);
        return CommonResponse.ok(updatePostId);
    }

    @Operation(
            summary = "커뮤니티 게시글 삭제",
            description = "커뮤니티 게시글 삭제")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "게시글 삭제 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 접근"),
            @ApiResponse(responseCode = "403", description = "존재하지 않는 게시글"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 카테고리")
    })
    @RequireUser
    @DeleteMapping("/{postId}")
    public CommonResponse<Long> deletePost(
            @Parameter(description = "게시글 ID", required = true) @PathVariable("postId") Long postId,
            @Parameter(hidden = true) PrincipalUser principalUser) {
        User user = blackUserService.checkBlackUser(principalUser);
        postService.deletePost(postId, user);
        return CommonResponse.ok(null);
    }

    @Operation(summary = "인기 글 조회", description = "모든 카테고리 중 기간 별 인기글을 조회힙니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "인기글 조회 성공"),
            @ApiResponse(responseCode = "500", description = "서버 에러 발생")
    })
    @GetMapping("/popular")
    public CommonResponse<PopularPostLiestRequestDTO> getPopularityPosts(
        @Parameter(description = "인기글 기간. WEEK(이번 주), MONTH(30일)", required = true) PopularPeriodType periodType) {
        PopularPostLiestRequestDTO result = postService.getPopularityPosts(periodType);
        return CommonResponse.ok(result);
    }
}
