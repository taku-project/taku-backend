package com.ani.taku_backend.common.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateImageDTO {

    private String fileName;

    private String originalFileName;

    private String fileType;

    private Integer fileSize;

    private Long uploadId;

    private String imageUrl;
    
}
