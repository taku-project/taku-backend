package com.ani.taku_backend.post.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
@Slf4j
public class PostListResponseDTO {

    @Schema(description = "해당 카테고리에 접속된 게시글 수(삭제된 글 제외)")
    private long postCount; // 게시글 수

    @Schema(description = "현재 페이지 번호(0부터 시작)")
    private int currentPage; // 현재 페이지 번호 추가

    @Schema(description = "전체 페이지 수(0부터 시작)")
    private int totalPages; //  전체 페이지 수 추가

    @Schema(description = "게시글 정보")
    private List<FindPostQueryDTO> responsePostList;

    public PostListResponseDTO(Page<FindPostQueryDTO> findAllPostQuerydslDTOList) {
        this.postCount = findAllPostQuerydslDTOList.getTotalElements();
        this.currentPage = findAllPostQuerydslDTOList.getNumber();
        this.totalPages = findAllPostQuerydslDTOList.getTotalPages();
        this.responsePostList = findAllPostQuerydslDTOList.getContent();
    }
}
