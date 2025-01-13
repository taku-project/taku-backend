package com.ani.taku_backend.post.controller;

import com.ani.taku_backend.common.enums.InteractionType;
import com.ani.taku_backend.common.response.CommonResponse;
import com.ani.taku_backend.post.service.PostInteractionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/community/posts")
public class PostInteractionController {

    private final PostInteractionService postInteractionService;

    /**
     * 게시글 좋아요/취소
     */

    @Operation(
            summary = "커뮤니티 게시글 좋아요",
            description = """
                    - 응답 성공시 게시글의 좋아요 수 반환
                    """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "좋아요 요청 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않는 접근"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 게시글"),
            @ApiResponse(responseCode = "429", description = "이전 요청 처리 중, 10초간 좋아요 lock")
    })
    @PostMapping("/{postId}/like")
    public CommonResponse<Long> postLikeInteraction(
            @Parameter(description = "게시글 ID") @PathVariable("postId") Long postId) {

        log.debug("좋아요 컨트롤러 시작");

        long postLikeCount = postInteractionService.togglePostLike(postId, null, InteractionType.LIKE);
        log.debug("좋아요 반영 성공");

        return CommonResponse.ok(postLikeCount);
    }
}
