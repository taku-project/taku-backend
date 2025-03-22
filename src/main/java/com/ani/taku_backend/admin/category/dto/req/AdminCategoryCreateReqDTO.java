package com.ani.taku_backend.admin.category.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminCategoryCreateReqDTO {
    @NotBlank
    private String categoryName;
    @NotNull
    private List<Long> aniGenreIds;
    @NotNull
    private MultipartFile image;
}
