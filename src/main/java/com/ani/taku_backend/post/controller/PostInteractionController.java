package com.ani.taku_backend.post.controller;

import com.ani.taku_backend.common.enums.InteractionType;
import com.ani.taku_backend.common.response.CommonResponse;
import com.ani.taku_backend.post.service.PostInteractionService;
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
    @PostMapping("/{postId}/like")
    public CommonResponse<Long> postLikeInteraction(
            @PathVariable("postId") Long postId) {
        log.debug("좋아요 컨트롤러 시작");

        long postLikeCount = postInteractionService.togglePostLike(postId, null, InteractionType.LIKE);
        log.debug("좋아요 반영 성공");

        return CommonResponse.ok(postLikeCount);
    }
}
