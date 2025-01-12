package com.ani.taku_backend.post.controller;

import com.ani.taku_backend.common.annotation.ViewCountChecker;
import com.ani.taku_backend.common.annotation.RequireUser;
import com.ani.taku_backend.common.response.CommonResponse;
import com.ani.taku_backend.post.model.dto.*;
import com.ani.taku_backend.post.service.PostReadService;
import com.ani.taku_backend.post.service.PostService;
import com.ani.taku_backend.user.model.dto.PrincipalUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @Operation(
            summary = "커뮤니티글 전체 조회",
            description = """
                     - postCount: 카테고리 내부 게시글 조회된 개수(삭제된 게시글 반영),
                     - responsePostList: 게시글 조회정보 리스트, 게시글 조회 성공 필드 확인
                     """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "게시글 조회 성공"),
    })
    @GetMapping
    public CommonResponse<PostListResponseDTO> findAllPostList(
            @Parameter(description = "커뮤니티 글 전체 조회 DTO", required = true) PostListRequestDTO postListRequestDTO) {
        log.info("postListRequestDTO: {}", postListRequestDTO.getSortFilterType());
        PostListResponseDTO findResultList = postService.findAllPostList(postListRequestDTO);
        return CommonResponse.ok(findResultList);
    }

    @Operation(
            summary = "커뮤니티 게시글 생성",
            description = """
                    - createPost: 판매글 정보를 포함한 JSON 데이터,
                    - postImage: 첨부할 이미지 파일 리스트 (이미지 파일, 필수값 아님)
                    """)
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "게시글 생성 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 접근"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 카테고리")
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        content = {
            @Content(
                mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                encoding = {
                    @Encoding(
                            name = "createPost",
                            contentType = "application/json"
                    ),
                    @Encoding(
                            name = "postImage",
                            contentType = "image/*"
                    )
                }
            )
        }
    )
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public CommonResponse<Long> createPost(
            @Parameter(
                    description = "커뮤니티 생성 요청 JSON 데이터", required = true
            )
            @Valid @RequestPart("createPost") PostCreateRequestDTO requestDTO,
            @Parameter(
                    description = "게시글 첨부 이미지(필수값 아님)"
            )
            @RequestPart(value = "postImage", required = false) List<MultipartFile> imageList) {

        Long createPostId = postService.createPost(requestDTO, imageList, null);
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

    @Operation(
            summary = "커뮤니티 게시글 수정",
            description = """
                    - updatePost: 게시글 업데이트 정보를 포함한 JSON 데이터
                    - updatePostImage: 첨부할 이미지 파일 리스트 (이미지 파일, 필수값 아님)
                    """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "게시글 수정 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 접근"),
            @ApiResponse(responseCode = "403", description = "존재하지 않는 게시글"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 카테고리")
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        content = {
            @Content(
                mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                encoding = {
                    @Encoding(
                            name = "updatePost",
                            contentType = "application/json"
                    ),
                    @Encoding(
                            name = "updatePostImage",
                            contentType = "image/*"
                    )
                }
            )
        }
    )
    @RequireUser
    @PutMapping(path ="/{postId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public CommonResponse<Long> updatePost(
            @Parameter(description = "게시글 ID", required = true) @PathVariable("postId") Long postId,
            @Parameter(
                    description = "판매글 업데이트 요청 JSON 데이터", required = true
            )
            @RequestPart("updatePost") @Valid PostUpdateRequestDTO requestDTO,
            @Parameter(
                    description = "새로 업로드할 이미지 파일"
            )
            @RequestPart(value = "updatePostImage") List<MultipartFile> imageList) {

        Long updatePostId = postService.updatePost(requestDTO, postId, imageList, null);
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
