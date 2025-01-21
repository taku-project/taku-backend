package com.ani.taku_backend.jangter.controller;

import com.ani.taku_backend.common.response.CommonResponse;
import com.ani.taku_backend.jangter.model.dto.ItemCategoriesResponseDTO;
import com.ani.taku_backend.jangter.service.ItemCategoriesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/itemCategory")
@RequiredArgsConstructor
public class ItemCategoriesController {

    private final ItemCategoriesService itemCategoriesService;

    @Operation(
            summary = "덕후장터 아이템 카테고리 ID 전체 조회",
            description = "api로 GET요청만 하면 덕후장터 아이템 카테고리 ID가 전체 조회됨(생성, 수정 시 사용)")
    @ApiResponses({
            @ApiResponse(responseCode = "200",description = "아이템 리스트 전송 성공")
    })
    @GetMapping
    public CommonResponse<ItemCategoriesResponseDTO> getAllItemCategories() {

        ItemCategoriesResponseDTO allItemCategoryIdList = itemCategoriesService.findAllItemCategories();

        return CommonResponse.ok(allItemCategoryIdList);
    }
}
