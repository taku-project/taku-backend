package com.ani.taku_backend.jangter.model.dto;

import com.ani.taku_backend.jangter.model.entity.ItemCategories;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ItemCategoriesResponseDTO {

    @Schema(description = "아이템 카테고리 id 전체 반환")
    private List<Long> itemCategoryIdList;

    @Schema(description = "아이템 카테고리 이름 전체 반환")
    private List<String> itemCategoryNameList;

    public ItemCategoriesResponseDTO(List<ItemCategories> itemCategoryNameList) {
        this.itemCategoryIdList = itemCategoryNameList.stream().map(ItemCategories::getId).collect(Collectors.toList());
        this.itemCategoryNameList = itemCategoryNameList.stream().map(ItemCategories::getName).collect(Collectors.toList());
    }
}
