package com.ani.taku_backend.post.controller;

import com.ani.taku_backend.common.model.MainResponse;
import com.ani.taku_backend.post.model.dto.PostResponseDTO;
import com.ani.taku_backend.post.model.dto.PostRequestDTO;
import com.ani.taku_backend.post.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

    @Operation(summary = "커뮤니티 게시글 조회(정렬, 검색(개발중))", description = """
            필터 조건, 검색어(개발 중), 정렬 순서에 따라 게시글 목록을 조회
            1. filter: 정렬 기준 선택
                - latest: 최신순  (기준 값: 게시글 Id)
                - likes : 좋아요순 (기준 값: 좋아요 수)
                - views : 조회수순 (기준 값: 조회수)
            
            2. lastValue: 선택 된 정렬 기준의 마지막 정수 값, 타입 Long
                - 정렬 기준이 최신 순이면 현재 데이터의 마지막 Id 값
                - 정렬 기준이 좋아요 순이면 현재 데이터의 마지막 좋아요 값
                - 정렬 기준이 조회수 순이면 현재 데이터의 마지막 조회수 값
            """
    )
    @GetMapping
    public ResponseEntity<MainResponse<List<PostResponseDTO>>> findAllPost(PostRequestDTO findAllPostParamDTO) {
        List<PostResponseDTO> posts = postService.findAllPost(
                                        findAllPostParamDTO.getFilter().toString(),
                                        findAllPostParamDTO.getLastValue(),
                                        findAllPostParamDTO.isAsc(),
                                        findAllPostParamDTO.getLimit(),
                                        findAllPostParamDTO.getKeyword());

        return ResponseEntity.ok(MainResponse.getSuccessResponse(posts));
    }
}
