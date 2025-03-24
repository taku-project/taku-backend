package com.ani.taku_backend.admin.category.dto.req;

import com.ani.taku_backend.category.domain.entity.CategoryStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class UpdateCategoryReqDTO {
    @NotNull
    private Long categoryId;
    @NotNull
    private CategoryStatus categoryStatus;
}
