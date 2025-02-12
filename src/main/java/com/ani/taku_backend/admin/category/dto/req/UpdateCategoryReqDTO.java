package com.ani.taku_backend.admin.category.dto.req;

import com.ani.taku_backend.category.domain.entity.CategoryStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class UpdateCategoryReqDTO {
    @Schema(description = "카테고리 상태")
    private CategoryStatus categoryStatus;
    @Schema(description = "카테고리 ID")
    private Long id;
}
