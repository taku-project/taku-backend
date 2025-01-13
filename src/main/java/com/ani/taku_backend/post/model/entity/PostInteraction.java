package com.ani.taku_backend.post.model.entity;

import com.ani.taku_backend.common.enums.InteractionType;
import com.ani.taku_backend.user.model.entity.User;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Id;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@JsonIgnoreProperties({"_class"})   // 직렬화 과정에서 _class 필드를 무시
@Document(collection = "posts_interaction")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class PostInteraction {

    @Id
    private String id;

    @Field(name = "postId")
    private Long postId;

    @Field(name = "userId")
    private Long userId;

    @Field(name = "type")
    private InteractionType type;

    @CreatedDate
    @Field(name = "created_at")
    private LocalDateTime createdAt;

    public static PostInteraction of(Post post, User user, InteractionType type) {
        return PostInteraction.builder()
                .postId(post.getId())
                .userId(user.getUserId())
                .type(type)
                .build();
    }

}
