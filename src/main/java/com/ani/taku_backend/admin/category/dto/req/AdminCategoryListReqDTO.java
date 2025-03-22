package com.ani.taku_backend.admin.category.dto.req;

import com.ani.taku_backend.admin.category.dto.CategorySearchType;
import com.ani.taku_backend.category.domain.entity.CategoryStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Pageable;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AdminCategoryListReqDTO {
    private Long lastId;
    private String keyword;
    private CategoryStatus status;
    private CategorySearchType searchType;
    @JsonIgnore
    private Pageable pageable;

    public void setPageable(Pageable pageable) {
        this.pageable = pageable;
    }
}
