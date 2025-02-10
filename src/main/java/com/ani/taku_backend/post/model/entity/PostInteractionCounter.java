package com.ani.taku_backend.post.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Id;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Document(collection = "posts_interaction_counter")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class PostInteractionCounter {

    @Id
    @Field("_id")
    private Long postId; // 게시글 ID

    @Field(name = "post_likes")
    private long postLikes; // 좋아요 수

    @Field(name = "category_id")
    private Long categoryId; // 카테고리 ID 추가

    @Field(name = "liked_user_ids")
    @Builder.Default
    private Set<Long> likedUserIds = new HashSet<>(); // 좋아요를 누른 사용자 ID 목록

    @Field(name = "deleted_at", write = Field.Write.ALWAYS)
    private LocalDateTime deletedAt; // 삭제 여부 (삭제되지 않았으면 null)

    public static PostInteractionCounter create(Post post) {
        return PostInteractionCounter.builder()
                .postId(post.getId())
                .postLikes(0L)
                .categoryId(post.getCategory().getId())
                .deletedAt(post.getDeletedAt())
                .likedUserIds(new HashSet<>())
                .build();
    }

}
