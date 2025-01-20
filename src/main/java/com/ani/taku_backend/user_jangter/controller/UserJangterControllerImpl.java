package com.ani.taku_backend.user_jangter.controller;

import com.ani.taku_backend.common.response.CommonResponse;
import com.ani.taku_backend.user.model.dto.PrincipalUser;
import com.ani.taku_backend.user.model.entity.User;
import com.ani.taku_backend.user_jangter.dto.res.UserCellResponseDTO;
import com.ani.taku_backend.user_jangter.dto.res.UserPurchaseResponseDTO;
import com.ani.taku_backend.user_jangter.service.UserJangterService;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user-janger")
@RequiredArgsConstructor
public class UserJangterControllerImpl implements UserJangterController {
    private final UserJangterService userJangterService;

    @GetMapping("/{userId}/purchase")
    public CommonResponse<PageImpl<UserPurchaseResponseDTO>> findUserPurchaseList(
            @AuthenticationPrincipal PrincipalUser principalUser,
            @PathVariable("userId") Long userId ,
            @ParameterObject Pageable pageable) {
        User user = principalUser.getUser();

        PageImpl<UserPurchaseResponseDTO> userPurchasePageList =  userJangterService.findUserPurchaseList(userId, pageable);

        return CommonResponse.ok(userPurchasePageList);
    }

    @GetMapping("/{userId}/sell")
    public CommonResponse<PageImpl<UserCellResponseDTO>> findUserCellList(
            @AuthenticationPrincipal PrincipalUser principalUser,
            @PathVariable("userId") Long userId ,
            @ParameterObject Pageable pageable) {
        PageImpl<UserCellResponseDTO> userPurchasePageList =  userJangterService.findUserCellList(userId, pageable);

        return CommonResponse.ok(userPurchasePageList);
    }
}
