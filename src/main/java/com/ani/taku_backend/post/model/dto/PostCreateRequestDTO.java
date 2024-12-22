package com.ani.taku_backend.post.model.dto;

import com.ani.taku_backend.common.model.dto.ImageCreateRequestDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostCreateRequestDTO {
    private Long CategoryId;
    private String title;
    private String content;
    private List<ImageCreateRequestDTO> imagelist;
}
