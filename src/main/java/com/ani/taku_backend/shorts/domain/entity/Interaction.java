package com.ani.taku_backend.shorts.domain.entity;

import com.ani.taku_backend.common.enums.InteractionType;
import com.ani.taku_backend.shorts.domain.vo.InteractionDetail;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "interactions")
@Getter
@Builder
@AllArgsConstructor
@ToString
public class Interaction<T extends InteractionDetail> {

    @Id
    private ObjectId id;
    
    @Field("user_id")
    private Long userId;
    @Field("shorts_id")
    private ObjectId shortsId;

    @Field("interaction_type")
    private InteractionType interactionType;
    @Field("shorts_tags")
    private List<String> shortsTags;
    @Field("details")
    private T details;

    @CreatedDate
    @Field("created_at")
    private LocalDateTime createdAt;

    public static Interaction createLike(Shorts shorts, Long userId) {
        return new Interaction(shorts, userId, InteractionType.LIKE, null);
    }

    public static Interaction createView(Shorts shorts, Long userId, InteractionDetail detail) {
        return new Interaction(shorts, userId, InteractionType.VIEW, detail);
    }

    private Interaction(Shorts shorts, Long userId, InteractionType type, InteractionDetail details) {
        this.shortsId = new ObjectId(shorts.getId());
        this.userId = userId;
        this.interactionType = type;
        this.shortsTags = shorts.getTags();
        this.details = (T)details;
    }
}

