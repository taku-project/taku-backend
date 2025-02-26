package com.ani.taku_backend.common.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomPageResponseDTO<T> {
    private List<T> content;          // 현재 페이지 컨텐츠
    private int totalPages;           // 전체 페이지 수
    private long totalElements;       // 전체 아이템 수
    private int number;               // 현재 페이지 번호

    public static <T> CustomPageResponseDTO<T> of(Page<T> page) {
        return CustomPageResponseDTO.<T>builder()
                .content(page.getContent())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .number(page.getNumber())
                .build();
    }
}