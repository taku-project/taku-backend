package com.ani.taku_backend.admin.category.dto.req;

import com.ani.taku_backend.category.domain.entity.CategoryStatus;
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
    private CategoryStatus status;
    private Long categoryId;
}
