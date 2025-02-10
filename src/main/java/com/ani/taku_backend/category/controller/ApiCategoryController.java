package com.ani.taku_backend.category.controller;

import com.ani.taku_backend.category.domain.dto.AniGenreListReqDTO;
import com.ani.taku_backend.category.domain.dto.RequestCategoryCreateDTO;
import com.ani.taku_backend.category.domain.dto.RequestCategorySearch;
import com.ani.taku_backend.category.domain.dto.ResponseCategoryDTO;
import com.ani.taku_backend.category.domain.dto.ResponseCategorySeachDTO;
import com.ani.taku_backend.category.service.CategoryService;
import com.ani.taku_backend.common.annotation.RequireUser;
import com.ani.taku_backend.common.exception.ExceptionDto;
import com.ani.taku_backend.common.response.CommonResponse;
import com.ani.taku_backend.common.service.FileService;
import com.ani.taku_backend.user.model.dto.PrincipalUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/category")
@RequiredArgsConstructor
@Slf4j
public class ApiCategoryController {

    private final CategoryService categoryService;
    private final FileService fileService;
    

    @Operation(
        summary = "카테고리 생성",
        description = "새로운 카테고리를 생성합니다. 카테고리 정보와 이미지를 함께 업로드해야 합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "카테고리 생성 성공", content = @Content(schema = @Schema(implementation = ResponseCategoryDTO.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(implementation = ExceptionDto.class))),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content(schema = @Schema(implementation = ExceptionDto.class))),
        @ApiResponse(responseCode = "404", description = "존재하지 않은 장르", content = @Content(schema = @Schema(implementation = ExceptionDto.class)))
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @RequireUser
    public CommonResponse<ResponseCategoryDTO> createCategory(
        @Valid RequestCategoryCreateDTO requestCategoryCreateDTO,
        @Parameter(hidden = true) PrincipalUser principalUser
    ){
        return CommonResponse.created(categoryService.createCategory(principalUser, requestCategoryCreateDTO));
    }

    @Operation(
        summary = "카테고리 검색",
        description = "카테고리를 검색합니다. 페이징과 정렬을 지원합니다."
    )
    @Parameters({
        @Parameter(
            name = "page",
            description = "페이지 번호 (0부터 시작)",
            in = ParameterIn.QUERY,
            schema = @Schema(type = "integer", defaultValue = "0")
        ),
        @Parameter(
            name = "size",
            description = "페이지 크기",
            in = ParameterIn.QUERY,
            schema = @Schema(type = "integer", defaultValue = "20")
        ),
        @Parameter(
            name = "sort",
            description = "정렬 기준 (예: name,asc 또는 name,desc)",
            in = ParameterIn.QUERY,
            schema = @Schema(type = "string", defaultValue = "name,asc")
        ),
        @Parameter(
            name = "name",
            description = "카테고리 이름으로 검색",
            in = ParameterIn.QUERY,
            schema = @Schema(type = "string")
        )
    })
    @GetMapping
    public CommonResponse<Page<ResponseCategorySeachDTO>> searchCategories(
        @Parameter(hidden = true) @ModelAttribute RequestCategorySearch requestCategorySearch,
        @Parameter(hidden = true) @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        Page<ResponseCategorySeachDTO> result = categoryService.searchCategories(requestCategorySearch, pageable);
        return CommonResponse.ok(result);
    }

    @Operation(
        summary = "카테고리 상세 조회",
        description = "카테고리 ID로 상세 정보를 조회합니다."
    )
    @Parameter(
        name = "id",
        description = "카테고리 ID",
        required = true,
        in = ParameterIn.PATH,
        schema = @Schema(type = "integer", format = "int64")
    )
    @GetMapping("/{id}")
    public CommonResponse<ResponseCategoryDTO> findCategoryById(
        @PathVariable("id") Long id
    ) {
        ResponseCategoryDTO result = categoryService.findCategoryById(id);
        return CommonResponse.ok(result);
    }

    @Operation(summary = "애니메이션 장르 목록 조회", description = "애니메이션 장르 목록을 조회합니다.")
    @GetMapping("/genres")
    public CommonResponse<AniGenreListReqDTO> findAniGenres(
        @Parameter(description = "검색 키워드", name = "keyword")
        @RequestParam(name = "keyword", required = false) String keyword) {
        AniGenreListReqDTO result = categoryService.findAniGenres(keyword);
        return CommonResponse.ok(result);
    }
}
