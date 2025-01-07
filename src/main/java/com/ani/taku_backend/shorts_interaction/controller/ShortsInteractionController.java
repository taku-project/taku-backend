package com.ani.taku_backend.shorts_interaction.controller;

import com.ani.taku_backend.common.exception.DuckwhoException;
import com.ani.taku_backend.common.exception.ErrorCode;
import com.ani.taku_backend.common.response.CommonResponse;
import com.ani.taku_backend.shorts_interaction.domain.dto.CreateShortsViewDTO;
import com.ani.taku_backend.shorts_interaction.domain.dto.req.CreateShortsViewReqDTO;
import com.ani.taku_backend.shorts_interaction.service.InteractionService;
import com.ani.taku_backend.user.model.dto.PrincipalUser;
import com.ani.taku_backend.user.model.entity.BlackUser;
import com.ani.taku_backend.user.model.entity.User;
import com.ani.taku_backend.user.service.BlackUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Shorts 상호작용 API", description = "Shorts 관련 좋아요, 싫어요 등 상호작용 API")
@RequestMapping("/api/shorts")
@RestController
@RequiredArgsConstructor
public class ShortsInteractionController {
    private final InteractionService interactionService;
    private final BlackUserService blackUserService;

    @Operation(summary = "Shorts 좋아요", description = "쇼츠 동영상에 로그인 한 유저가 좋아요를 누름")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "쇼츠 좋아요 생성 완료."),
        @ApiResponse(responseCode = "403", description = "회원 인증이 되지 않았습니다."),
        @ApiResponse(responseCode = "404", description = "쇼츠 정보를 찾을 수 없습니다.")
    })
    @PostMapping("/{shortsId}/likes")
    public CommonResponse<Void> addLike(@AuthenticationPrincipal PrincipalUser userPrincipal,
        @Parameter(description = "쇼츠 아이디", required = true) @PathVariable("shortsId") String shortsId) {
        User user = userPrincipal.getUser();
        validateBlackUser(user.getUserId());

        interactionService.addLike(user, shortsId);

        return CommonResponse.ok(null);
    }

    @Operation(summary = "Shorts 좋아요 취소", description = "쇼츠 동영상에 좋아요한 유저가 취소를 누름")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "쇼츠 좋아요 취소간 다 완료."),
            @ApiResponse(responseCode = "403", description = "회원 인증이 되지 않았습니다."),
            @ApiResponse(responseCode = "404", description = "쇼츠 정보를 찾을 수 없습니다.")
    })
    @PostMapping("/{shortsId}/likes/cancel")
    public CommonResponse<Void> cancelLike(@AuthenticationPrincipal PrincipalUser userPrincipal,
        @Parameter(description = "쇼츠 아이디", required = true) @PathVariable("shortsId") String shortsId) {
        User user = userPrincipal.getUser();
        validateBlackUser(user.getUserId());

        interactionService.cancelLike(user, shortsId);

        return CommonResponse.ok(null);
    }

    @Operation(summary = "Shorts 시청 기록", description = "사용자가 쇼츠를 시청한 데이터 추가. 다음 쇼츠로 넘어가거나 페이지를 벗어날 때 사용.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "쇼츠 좋아요 취소간 다 완료."),
            @ApiResponse(responseCode = "403", description = "회원 인증이 되지 않았습니다."),
            @ApiResponse(responseCode = "404", description = "쇼츠 정보를 찾을 수 없습니다.")
    })
    @PostMapping("/{shortsId}/view")
    public CommonResponse<Void> createView(@AuthenticationPrincipal PrincipalUser userPrincipal,
        @Parameter(description = "쇼츠 아이디", required = true) @PathVariable("shortsId") String shortsId,
        @RequestBody CreateShortsViewReqDTO createShortsViewReqDTO) {
        User user = userPrincipal.getUser();
        validateBlackUser(user.getUserId());

        CreateShortsViewDTO createShortsViewDTO = CreateShortsViewDTO.builder()
                .shortsId(shortsId)
                .viewDuration(createShortsViewReqDTO.getViewTime())
                .playDuration(createShortsViewReqDTO.getPlayTime())
                .user(user)
                .build();
        interactionService.createView(createShortsViewDTO);

        return CommonResponse.ok(null);
    }

    private void validateBlackUser(Long userId) {
        List<BlackUser> blackUser = blackUserService.findByUserId(userId);
        if(!blackUser.isEmpty()) {
            throw new DuckwhoException(ErrorCode.BLACK_USER);
        }
    }
}
