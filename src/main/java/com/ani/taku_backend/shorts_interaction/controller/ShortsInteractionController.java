package com.ani.taku_backend.shorts_interaction.controller;

import com.ani.taku_backend.common.exception.DuckwhoException;
import com.ani.taku_backend.common.exception.ErrorCode;
import com.ani.taku_backend.common.response.CommonResponse;
import com.ani.taku_backend.shorts_interaction.service.InteractionService;
import com.ani.taku_backend.user.model.dto.PrincipalUser;
import com.ani.taku_backend.user.model.entity.BlackUser;
import com.ani.taku_backend.user.model.entity.User;
import com.ani.taku_backend.user.service.BlackUserService;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequestMapping("/api/shorts")
@RestController
@RequiredArgsConstructor
public class ShortsInteractionController {
    private final InteractionService interactionService;
    private final BlackUserService blackUserService;

    @PostMapping("/{shortsId}/likes")
    public CommonResponse<Void> addLike(@AuthenticationPrincipal PrincipalUser userPrincipal,
        @Parameter(description = "쇼츠 아이디", required = true) @PathVariable("shortsId") String shortsId) {
        User user = userPrincipal.getUser();
        validateBlackUser(user.getUserId());

        interactionService.addLike(user, shortsId);

        return CommonResponse.ok(null);
    }

    @PostMapping("/{shortsId}/likes/cancel")
    public CommonResponse<Void> cancelLike(@AuthenticationPrincipal PrincipalUser userPrincipal,
        @Parameter(description = "쇼츠 아이디", required = true) @PathVariable("shortsId") String shortsId) {
        User user = userPrincipal.getUser();
        validateBlackUser(user.getUserId());

        interactionService.cancelLike(user, shortsId);

        return CommonResponse.ok(null);
    }

    private void validateBlackUser(Long userId) {
        List<BlackUser> blackUser = blackUserService.findByUserId(userId);
        if(!blackUser.isEmpty()) {
            throw new DuckwhoException(ErrorCode.BLACK_USER);
        }
    }
}
