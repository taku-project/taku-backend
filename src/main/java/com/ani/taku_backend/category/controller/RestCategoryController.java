package com.ani.taku_backend.category.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ani.taku_backend.category.domain.dto.RequestCategoryCreateDTO;
import com.ani.taku_backend.category.domain.dto.ResponseCategoryDTO;
import com.ani.taku_backend.category.service.CategoryService;
import com.ani.taku_backend.common.annotation.RequireUser;
import com.ani.taku_backend.common.model.dto.CreateImageDTO;
import com.ani.taku_backend.common.service.FileService;
import com.ani.taku_backend.common.util.FileUtil;
import com.ani.taku_backend.global.exception.CustomException;
import com.ani.taku_backend.global.exception.ErrorCode;
import com.ani.taku_backend.global.exception.ExceptionDto;
import com.ani.taku_backend.global.response.ApiResponse;
import com.ani.taku_backend.user.model.dto.PrincipalUser;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/category")
@RequiredArgsConstructor
@Slf4j
public class RestCategoryController {

    private final CategoryService categoryService;
    private final FileService fileService;
    

    @Operation(
        summary = "카테고리 생성",
        description = "새로운 카테고리를 생성합니다. 카테고리 정보와 이미지를 함께 업로드해야 합니다."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "카테고리 생성 성공",
            content = @Content(schema = @Schema(implementation = ResponseCategoryDTO.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "잘못된 요청",
            content = @Content(schema = @Schema(implementation = ExceptionDto.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "인증되지 않은 사용자",
            content = @Content(schema = @Schema(implementation = ExceptionDto.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "존재하지 않은 장르",
            content = @Content(schema = @Schema(implementation = ExceptionDto.class))
        )
    })
    @Parameters({
        @Parameter(
            name = "Authorization",
            in = ParameterIn.HEADER,
            required = true,
            description = "JWT 토큰",
            example = "Bearer eyJhbGciOiJIUzI1NiJ9.eyJhZ2VfcmFuZ2..."
        )
    })
	@io.swagger.v3.oas.annotations.parameters.RequestBody(
		content = {
			@Content(
				mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
				encoding = {
					@Encoding(
						name = "category", 
						contentType = "application/json"
					),
					@Encoding(
						name = "image",
						contentType = "image/png"
					)
				}
			)
		}
	)
    @PostMapping(value = "" , consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @RequireUser
    public ApiResponse<ResponseCategoryDTO> createCategory(
        @RequestPart("category")@Parameter(description = "카테고리 정보 <code>RequestCategoryCreateDTO</code> 스키마 참고 <code>Content-Type: application/json</code>") RequestCategoryCreateDTO requestCategoryCreateDTO,
        @RequestPart("image")@Parameter(description = "카테고리 이미지") MultipartFile image,
        @Parameter(hidden = true) PrincipalUser principalUser
    ){
        return ApiResponse.created(categoryService.createCategory(principalUser, requestCategoryCreateDTO, image));
    }
    
}
