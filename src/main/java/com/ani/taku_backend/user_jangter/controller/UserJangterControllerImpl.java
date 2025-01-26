package com.ani.taku_backend.user_jangter.controller;

import com.ani.taku_backend.common.response.CommonResponse;
import com.ani.taku_backend.user.model.dto.PrincipalUser;
import com.ani.taku_backend.user.model.entity.User;
import com.ani.taku_backend.user_jangter.dto.UserPurchaseResponseDTO;
import com.ani.taku_backend.user_jangter.service.UserJangterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
    public CommonResponse<PageImpl<UserPurchaseResponseDTO>> findUserPurchases(
            @AuthenticationPrincipal PrincipalUser principalUser,
            @PathVariable("userId") Long userId ,
            @ParameterObject
            Pageable pageable) {
        User user = principalUser.getUser();
//        CompleteJangterSortType.ID;
        PageImpl<UserPurchaseResponseDTO> userPurchasePageList =  userJangterService.findUserPurchaseList(userId, pageable);

        return CommonResponse.ok(userPurchasePageList);
    }
}
