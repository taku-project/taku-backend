package com.ani.taku_backend.admin.domain.dto;

import java.time.LocalDateTime;

import com.ani.taku_backend.admin.domain.entity.ProfanityFilter;
import com.ani.taku_backend.common.enums.StatusType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 조회 응답 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResponseProfannityDTO {

    private Long id;
    private Long userId;
    private String nickname;

    private String keyword;
    private String explaination;
    private StatusType status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ResponseProfannityDTO of(ProfanityFilter profanityFilter) {
        return ResponseProfannityDTO.builder()
                .id(profanityFilter.getId())
                .userId(profanityFilter.getAdmin().getUserId())
                .nickname(profanityFilter.getAdmin().getNickname())
                .keyword(profanityFilter.getKeyword())
                .explaination(profanityFilter.getExplaination())
                .status(profanityFilter.getStatus())
                .createdAt(profanityFilter.getCreatedAt())
                .updatedAt(profanityFilter.getUpdatedAt())
                .build();
    }
    
}
