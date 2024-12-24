package com.ani.taku_backend.post.controller;

import com.ani.taku_backend.common.annotation.RequireUser;
import com.ani.taku_backend.common.model.MainResponse;
import com.ani.taku_backend.post.model.dto.PostCreateRequestDTO;
import com.ani.taku_backend.post.model.dto.PostListResponseDTO;
import com.ani.taku_backend.post.model.dto.PostListRequestDTO;
import com.ani.taku_backend.post.model.dto.PostUpdateRequestDTO;
import com.ani.taku_backend.post.service.PostService;
import com.ani.taku_backend.user.model.dto.PrincipalUser;
import com.ani.taku_backend.user.model.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

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
    public ResponseEntity<MainResponse<List<PostListResponseDTO>>> findAllPost(PostListRequestDTO requestDTO) {
        List<PostListResponseDTO> posts = postService.findAllPost(
                requestDTO.getFilter().toString(),
                requestDTO.getLastValue(),
                requestDTO.isAsc(),
                requestDTO.getLimit(),
                requestDTO.getKeyword(),
                requestDTO.getCategoryId());

        return ResponseEntity.ㅊ(MainResponse.getSuccessResponse(posts));
    }

    @Operation(summary = "커뮤니티 게시글 생성", description = """
            1. categoryId: User가 접속한 카테고리 ID
            2. title: 작성한 제목
            3. content: 작성한 내용
            4. imageList: 업로드한 이미지 정보
                - 이미지 파일이름
                - 이미지 저장 경로
                - 원본 파일명
                - 타입
                - 크기
            """)
    @RequireUser
    @PostMapping("/{id}")
    public ResponseEntity<MainResponse<Long>> createPost(@PathVariable Long id,
                                           PrincipalUser principalUser,
                                           @Valid @RequestBody PostCreateRequestDTO requestDTO) {
        Long postId = postService.createPost(requestDTO, principalUser);
        return ResponseEntity.created(MainResponse.getSuccessResponse(postId));
    }

    // TODO: 게시글 상세 조회 필요
    // findPostById

    @RequireUser
    @PutMapping("/{id}")
    public ResponseEntity<MainResponse<Long>> updatePost(@PathVariable Long id,
                                           PrincipalUser principalUser,
                                           @Valid @RequestBody PostUpdateRequestDTO requestDTO) {
        Long updatePostId = postService.updatePost(requestDTO, principalUser, id);
        return ResponseEntity.ok(MainResponse.getSuccessResponse(updatePostId));
    }

    @RequireUser
    @DeleteMapping("/{id}")
    public ResponseEntity<MainResponse<Void>> deletePost(@PathVariable Long id,
                                                                        PrincipalUser principalUser) {
        postService.deletePost(id, principalUser);
        return ResponseEntity.noContent().build();
    }
}
