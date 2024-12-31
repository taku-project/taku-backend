package com.ani.taku_backend.shorts.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ShortsCreateReqDTO {
    @NotNull
    private MultipartFile file;
    @NotBlank
    private String title;
    @NotBlank
    private String description;
    private List<String> tags;
}
