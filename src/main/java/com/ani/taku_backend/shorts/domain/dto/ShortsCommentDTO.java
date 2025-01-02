package com.ani.taku_backend.shorts.domain.dto;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import com.ani.taku_backend.user.model.entity.User;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "쇼츠 댓글 응답 DTO")
public class ShortsCommentDTO {

    @Schema(description = "쇼츠 댓글 아이디")
    private String id;

    @Schema(description = "쇼츠 댓글 내용")
    private String comment;

    @Schema(description = "쇼츠 아이디")
    @JsonProperty("shorts_id")
    private String shortsId;

    @Schema(description = "쇼츠 댓글 생성 시간" , example = "2024-01-01 00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", shape = JsonFormat.Shape.STRING)
    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @Schema(description = "쇼츠 댓글 작성자 정보")
    @JsonProperty("user_info")
    private CommentUserDTO userInfo;

    @Schema(description = "쇼츠 댓글 대댓글 목록")
    @JsonProperty("replies")
    private List<CommentReplyDTO> replies;

    public void setCreatedAt(LocalDateTime createdAt) {
        // ISO 형식의 날짜를 원하는 포맷으로 변환
        this.createdAt = createdAt.truncatedTo(ChronoUnit.MINUTES);
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "쇼츠 댓글 작성자 정보")
    public static class CommentUserDTO {
        @Schema(description = "쇼츠 댓글 작성자 아이디")
        private Long id;
        @Schema(description = "쇼츠 댓글 작성자 닉네임")
        private String nickname;

        @Schema(description = "쇼츠 댓글 작성자 프로필 이미지")
        @JsonProperty("profile_image")
        private String profileImage;

        // 유저 엔티티를 CommentUserDTO로 변환
        public static CommentUserDTO of(User user) {
            return CommentUserDTO.builder()
                .id(user.getUserId())
                .nickname(user.getNickname())
                .profileImage(user.getProfileImg())
                .build();
        }
    }

    public static class CommentReplyDTO{
        @Schema(description = "쇼츠 댓글 대댓글 아이디")
        @JsonProperty("id")
        private String id;
        @JsonProperty("reply_text")
        @Schema(description = "쇼츠 댓글 대댓글 내용")
        private String replyText;

        @JsonProperty("user_id")
        @Schema(description = "쇼츠 댓글 대댓글 작성자 아이디")
        private Long userId;

        @JsonProperty("created_at")
        @Schema(description = "쇼츠 댓글 대댓글 생성 시간")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm", shape = JsonFormat.Shape.STRING)
        private LocalDateTime createdAt;
    }
}
