package com.ani.taku_backend.jangter.controller;

import com.ani.taku_backend.common.annotation.RequireUser;
import com.ani.taku_backend.common.response.CommonResponse;
import com.ani.taku_backend.jangter.model.dto.ProductStatusUpdateRequestDTO;
import com.ani.taku_backend.jangter.service.ProductStatusService;
import com.ani.taku_backend.user.model.dto.PrincipalUser;
import com.ani.taku_backend.user.model.entity.User;
import com.ani.taku_backend.user.service.BlackUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 상품 상태 관리 컨트롤러
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/jangter")
public class ProductStatusController {
    private final ProductStatusService productStatusService;
    private final BlackUserService blackUserService;

    @Operation(summary = "상품 상태 변경", description = "상품의 판매 상태를 변경합니다 (판매중, 예약중, 판매완료)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "상태 변경 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 접근"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 상품")
    })
    @RequireUser
    @PutMapping("/{productId}/status")
    public CommonResponse<Void> updateProductStatus(
            @Parameter(description = "상품 ID", required = true)
            @PathVariable Long productId,
            
            @Parameter(description = "변경할 상태 정보", required = true)
            @RequestBody @Valid ProductStatusUpdateRequestDTO request,
            
            @Parameter(hidden = true)
            @AuthenticationPrincipal PrincipalUser principalUser
    ) {
        User user = blackUserService.checkBlackUser(principalUser);
        productStatusService.updateProductStatus(productId, request.getStatus(), user);
        return CommonResponse.ok(null);
    }
} 