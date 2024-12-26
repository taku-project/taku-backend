package com.ani.taku_backend.admin.domain.dto;

import java.time.LocalDateTime;

import com.ani.taku_backend.admin.domain.entity.ProfanityFilter;
import com.ani.taku_backend.common.enums.StatusType;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ResponseCreateProfanityDTO {
    private Long id;
    private Long userId;
    private String keyword;
    private String explaination;
    private StatusType status;
    private LocalDateTime createdAt;

    public static ResponseCreateProfanityDTO of(ProfanityFilter profanityFilter) {
        return ResponseCreateProfanityDTO.builder()
            .id(profanityFilter.getId())
            .userId(profanityFilter.getAdmin().getUserId())
            .keyword(profanityFilter.getKeyword())
            .explaination(profanityFilter.getExplaination())
            .status(profanityFilter.getStatus())
            .createdAt(profanityFilter.getCreatedAt())
            .build();
    }
}
