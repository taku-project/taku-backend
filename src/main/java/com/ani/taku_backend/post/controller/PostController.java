package com.ani.taku_backend.post.controller;

import com.ani.taku_backend.common.model.MainResponse;
import com.ani.taku_backend.post.model.dto.PostListResponseDTO;
import com.ani.taku_backend.post.model.dto.PostListRequestDTO;
import com.ani.taku_backend.post.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    public ResponseEntity<MainResponse<List<PostListResponseDTO>>> findAllPost(PostListRequestDTO findAllPostParamDTO) {
        List<PostListResponseDTO> posts = postService.findAllPost(
                                        findAllPostParamDTO.getFilter().toString(),
                                        findAllPostParamDTO.getLastValue(),
                                        findAllPostParamDTO.isAsc(),
                                        findAllPostParamDTO.getLimit(),
                                        findAllPostParamDTO.getKeyword(),
                                        findAllPostParamDTO.getCategoryId());

        return ResponseEntity.ok(MainResponse.getSuccessResponse(posts));
    }

    @PostMapping("/{id}")
    public ResponseEntity<MainResponse<PostListResponseDTO>> createPost() {
        return null;
    }

    // TODO: 게시글 상세 조회 필요
    // findPostByUserId

    @PutMapping("/{id}")
    public ResponseEntity<MainResponse<PostListResponseDTO>> updatePost() {
        return null;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<MainResponse<PostListResponseDTO>> deletePost() {
        return null;
    }
}
