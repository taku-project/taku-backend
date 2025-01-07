package com.ani.taku_backend.shorts.domain.entity;

import java.time.LocalDateTime;
import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import com.ani.taku_backend.common.enums.InteractionType;
import com.ani.taku_backend.shorts.domain.vo.InteractionDetail;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Document(collection = "interactions")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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

    public static Interaction create(Shorts shorts, Long userId, InteractionType interactionType, InteractionDetail details) {
        return Interaction.builder()
                .shortsId(new ObjectId(shorts.getId()))
                .userId(userId)
                .interactionType(interactionType)
                .shortsTags(shorts.getTags())
                .details(details)
                .build();
    }

    public static Interaction create(Shorts shorts, Long userId, InteractionType interactionType) {
        return Interaction.builder()
                .shortsId(new ObjectId(shorts.getId()))
                .userId(userId)
                .interactionType(interactionType)
                .shortsTags(shorts.getTags())
                .build();
    }
}

