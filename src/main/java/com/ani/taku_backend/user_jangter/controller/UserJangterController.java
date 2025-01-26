package com.ani.taku_backend.user_jangter.controller;

import com.ani.taku_backend.common.response.CommonResponse;
import com.ani.taku_backend.user.model.dto.PrincipalUser;
import com.ani.taku_backend.user_jangter.dto.UserPurchaseResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@Tag(name = "유저 장터 API", description = "유저가 장터에서 활동한 ")
public interface UserJangterController {
    @Operation(summary = "유저 장터 상품 구매 목록", description = "유저가 장터에서 구매한 물품의 목록들을 보여줍니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "API 요청 성공"),
        @ApiResponse(responseCode = "400", description = "입력한 값의 유효성이 올바르지 않을 때"),
        @ApiResponse(responseCode = "403", description = "사용자 로그인이 되어있지 않았을 때")
    })
    @Parameters(value = {
        @Parameter(name = "page", description = "페이지 번호 (0부터 시작)", in = ParameterIn.QUERY, schema = @Schema(type = "integer", defaultValue = "0")),
        @Parameter(name = "size",description = "페이지 크기", in = ParameterIn.QUERY, schema = @Schema(type = "integer", defaultValue = "20")),
        @Parameter(name = "sort", description = "정렬 기준 ID, TITLE, CATEGORY_NAME, PRICE (예: ID,ASC || TITLE,DESC). ", in = ParameterIn.QUERY, schema = @Schema(type = "string", defaultValue = "id,desc")),
    })
    CommonResponse<PageImpl<UserPurchaseResponseDTO>> findUserPurchases(PrincipalUser principalUser, Long userId , Pageable pageable);
}
