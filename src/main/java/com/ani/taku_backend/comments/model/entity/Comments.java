package com.ani.taku_backend.comments.model.entity;

import com.ani.taku_backend.common.baseEntity.BaseTimeEntity;
import com.ani.taku_backend.post.model.entity.Post;
import com.ani.taku_backend.user.model.entity.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

@Builder
@Entity
@Table(name = "comments")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Slf4j
public class Comments extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_comment_id")
    private Comments parentComment;

    @Column(length = 255, nullable = false)
    private String content;

    @Column(length = 50, nullable = false)
    @Builder.Default
    private String status = "ACTIVE";     // 기본값 설정 ACTIVE, -> 관리자가 수정?

    private LocalDateTime deletedAt;

    // 댓글 생성 스태틱 메서드
    public static Comments createComments(User user, Post post, String content) {
        return Comments.builder()
                .user(user)
                .post(post)
                .content(content)
                .build();
    }

    // 댓글의 댓글 생성 스태틱 메서드
    public static Comments createCommentsReply(User user, Post post, Comments parentComment, String content) {
        return Comments.builder()
                .user(user)
                .post(post)
                .parentComment(parentComment)
                .content(content)
                .build();

    }

    // 본문 업데이트 메서드
    public void updateComments(String content) {
        log.info("댓글 수정 전, 기존 댓글: {}, 수정 댓글: {}", this.content, content);
        this.content = content;
        log.info("댓글 수정 전, 기존 댓글: {}, 수정 댓글: {}", this.content, content);
    }

    // 삭제
    public void delete() {
        deletedAt = LocalDateTime.now();
    }
}
