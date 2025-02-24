package com.ani.taku_backend.comments.model.dto;

import com.ani.taku_backend.comments.model.entity.Comments;
import com.ani.taku_backend.user.model.dto.UserDetailDTO;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

public record CommentsResponseDTO(
        @Schema(description = "댓글 ID")
        Long id,

        @Schema(description = "댓글 내용")
        String content,

        @Schema(description = "댓글 작성 시간", example = "2024-03-19 14:30:00")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime createdAt,

        @Schema(description = "댓글 작성자 정보")
        UserDetailDTO user,

        @Schema(description = "댓글 소유자 여부")
        boolean isOwner,

        @Schema(description = "대댓글 목록")
        List<CommentsResponseDTO> replies
) {
    public static CommentsResponseDTO of(Comments comment, Long currentUserId, List<CommentsResponseDTO> replies) {
        return new CommentsResponseDTO(
                comment.getId(),
                comment.getContent(),
                comment.getCreatedAt(),
                UserDetailDTO.builder()
                        .nickname(comment.getUser().getNickname())
                        .profileImg(comment.getUser().getProfileImg())
                        .gender(comment.getUser().getGender())
                        .ageRange(comment.getUser().getAgeRange())
                        .build(),
                currentUserId != null && currentUserId.equals(comment.getUser().getUserId()),
                replies
        );
    }

    public static CommentsResponseDTO of(Comments comment, Long currentUserId) {
        return of(comment, currentUserId, List.of());
    }
}