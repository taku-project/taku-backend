package com.ani.taku_backend.common.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImageUpdateRequestDTO {
    private Long id;
    private String fileName;
    private String imageUrl;
    private String originalFileName;
    private String fileType;
    private Integer fileSize;
}
