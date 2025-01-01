package com.ani.taku_backend.post.controller;

import com.ani.taku_backend.annotation.ViewCountChecker;
import com.ani.taku_backend.common.annotation.RequireUser;
import com.ani.taku_backend.common.response.ApiResponse;
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

    @Operation(summary = "커뮤니티 게시글 조회(정렬, 검색)", description = """
            필터 조건, 검색어, 정렬 순서에 따라 카테고리 별 게시글 목록을 조회
            1. filter: 정렬 기준 선택
                - latest: 최신순  (기준 값: 게시글 Id)
                - likes : 좋아요순 (기준 값: 좋아요 수)
                - views : 조회수순 (기준 값: 조회수)
            
            2. lastValue: 선택 된 정렬 기준의 마지막 정수 값, 타입 Long
                - 정렬 기준이 최신 순이면 현재 데이터의 마지막 Id 값
                - 정렬 기준이 좋아요 순이면 현재 데이터의 마지막 좋아요 값
                - 정렬 기준이 조회수 순이면 현재 데이터의 마지막 조회수 값
            
            3. isAsc: 내림차순, 오름차순 여부
                - false: 기본값, 내림차순
                - true: 오름차순
            
            4. limit: 출력 페이지 개수
                - 20개 (피그마에서는 5개였으나 확인이 필요함)
            
            5. keyword: 게시글 제목 + 내용 검색어
            
            6. categoryId: 게시글이 속한 카테고리 ID
            """
    )
    @GetMapping
    public ApiResponse<List<PostListResponseDTO>> findAllPost(PostListRequestDTO requestDTO) {
        List<PostListResponseDTO> postList = postService.findAllPost(
                requestDTO.getFilter().toString(),
                requestDTO.getLastValue(),
                requestDTO.isAsc(),
                requestDTO.getLimit(),
                requestDTO.getKeyword(),
                requestDTO.getCategoryId());

        return ApiResponse.ok(postList);
    }

    @Operation(summary = "커뮤니티 게시글 생성", description = """
            createPost(type: application/json)
            1. categoryId: 접속한 카테고리 ID
            2. title: 작성한 제목
            3. content: 작성한 내용
            4. imageList: 업로드한 이미지 정보(List)
                1) originalFileName: 업로드 파일명
                2) fileType: 파일 타입
                3) fileSize: 크기
           
            postImage(type: multipartFile)
            - 이미지데이터
            """)
    @RequireUser
    @PostMapping
    public ApiResponse<Long> createPost(
            PrincipalUser principalUser,
            @Valid @RequestPart("createPost") PostCreateUpdateRequestDTO requestDTO,
            @RequestPart(value = "postImage", required = false) List<MultipartFile> imageList) {

        Long createPostId = postService.createPost(requestDTO, principalUser, imageList);
        return ApiResponse.created(createPostId);
    }

    @Operation(summary = "커뮤니티 게시글 상세 조회", description = """
            1. 게시글 상세 조회(댓글은 나중에 작업)
            """)
    @GetMapping("/{postId}")
    public ApiResponse<PostDetailResponseDTO> findPostDetail(
            @PathVariable Long postId,
            @ViewCountChecker Boolean canAddView,
            PrincipalUser principalUser
    ) {
        Long currentUserId = null;
        if (principalUser != null) {
            currentUserId = principalUser.getUserId();
        }

        PostDetailResponseDTO detail = postReadService.getPostDetail(postId, canAddView, currentUserId);
        return ApiResponse.ok(detail);
    }

    @Operation(summary = "커뮤니티 게시글 수정", description = """
            updatePost(type: application/json)
            1. categoryId: 접속한 카테고리 ID
            2. title: 작성한 제목
            3. content: 작성한 내용
            4. imageList: 업로드한 이미지 정보(List)
                1) originalFileName: 업로드 파일명
                2) fileType: 파일 타입
                3) fileSize: 크기
           
            postImage(type: multipartFile)
            - 이미지데이터
            """)
    @RequireUser
    @PutMapping("/{postId}")
    public ApiResponse<Long> updatePost(@PathVariable Long postId,
                                        PrincipalUser principalUser,
                                        @Valid @RequestPart("updatePost") PostCreateUpdateRequestDTO requestDTO,
                                        @RequestPart(value = "postImage", required = false) List<MultipartFile> imageList) {
        Long updatePostId = postService.updatePost(requestDTO, principalUser, postId, imageList);
        return ApiResponse.ok(updatePostId);
    }

    @Operation(summary = "커뮤니티 게시글 삭제")
    @RequireUser
    @DeleteMapping("/{postId}")
    public ApiResponse<Long> deletePost(@PathVariable Long postId,
                                        PrincipalUser principalUser) {
        Long deletePostId = postService.deletePost(postId, principalUser);
        return ApiResponse.ok(deletePostId);
    }
}
