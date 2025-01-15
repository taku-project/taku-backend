package com.ani.taku_backend.shorts_interaction.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InteractionResponse {
    private UserInteractionResponse like;
    private UserInteractionResponse dislike;

    public static InteractionResponse fromMap(Map<String, Object> map) {
        if(map == null) return null;
        Map like = (Map) map.getOrDefault("like", null);
        Map dislike = (Map) map.getOrDefault("dislike", null);
        return InteractionResponse.builder()
                .like(UserInteractionResponse.fromMap(like))
                .dislike(UserInteractionResponse.fromMap(dislike))
                .build();
    }
}
