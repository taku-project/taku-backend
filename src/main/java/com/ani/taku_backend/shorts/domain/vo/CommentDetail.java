package com.ani.taku_backend.shorts.domain.vo;

import java.time.LocalDateTime;
import java.util.List;

import org.bson.types.ObjectId;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 쇼츠 댓글 상세 정보 (댓글 내용, 대댓글 목록)
 */
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class CommentDetail {

    private String commentText;
    private List<Reply> replies;
    

    @Getter
    @Builder
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    public static class Reply {
        private ObjectId id;
        private Long userId;
        private String replyText;
        private LocalDateTime createdAt;
    }
}
