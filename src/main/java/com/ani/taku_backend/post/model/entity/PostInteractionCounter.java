package com.ani.taku_backend.post.model.entity;

import com.ani.taku_backend.common.enums.InteractionType;
import com.ani.taku_backend.common.exception.DuckwhoException;
import com.ani.taku_backend.shorts.domain.entity.Interaction;
import com.ani.taku_backend.shorts.domain.entity.Shorts;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Id;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import static com.ani.taku_backend.common.exception.ErrorCode.NOT_FOUND_INTERACTION;

@JsonIgnoreProperties({"_class"})   // 직렬화 과정에서 _class 필드를 무시
@Document(collection = "posts_interaction_counter")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class PostInteractionCounter {

    @Id
    @Field("_id")  // MongoDB의 _id 필드로 매핑
    private Long postId; // 게시글 ID

    @Field(name = "post_likes")
    private long postLikes; // 좋아요 수

    public static PostInteractionCounter create(Long postId) {
        return PostInteractionCounter.builder()
                .postId(postId)
                .postLikes(0L)
                .build();
    }

}
