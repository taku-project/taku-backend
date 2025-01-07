package com.ani.taku_backend.post.model.dto;

import com.ani.taku_backend.common.model.entity.Image;
import com.ani.taku_backend.post.model.entity.Post;
import com.ani.taku_backend.post.repository.impl.dto.FindAllPostQuerydslDTO;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.querydsl.core.annotations.QueryProjection;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class PostListResponseDTO {

    @Schema(description = "해당 카테고리에 접속된 게시글 수(삭제된 글 제외)")
    private long postCount; // 게시글 수

    @Schema(description = "게시글 정보")
    private List<FindAllPostQuerydslDTO> responsePostList;

    public PostListResponseDTO(long postCount, List<FindAllPostQuerydslDTO> findAllPostQuerydslDTOList) {
        this.postCount = postCount;
        this.responsePostList = findAllPostQuerydslDTOList;
    }
}
