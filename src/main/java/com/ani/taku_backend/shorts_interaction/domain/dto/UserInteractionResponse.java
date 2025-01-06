package com.ani.taku_backend.shorts_interaction.domain.dto;

import com.ani.taku_backend.shorts.domain.entity.InteractionField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@Schema(description = "사용자의 상호작용 정보")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class UserInteractionResponse {
    private String id;
    private String shortsId;
    private Long userId;

    public static UserInteractionResponse fromMap(Map<String, Object> map) {
        if(map == null || map.isEmpty()) return null;
        return UserInteractionResponse.builder()
                .id(map.getOrDefault(InteractionField.ID.getFieldName(), null).toString())
                .shortsId(map.getOrDefault(InteractionField.SHORTS_ID.getFieldName(), null).toString())
                .userId(Long.valueOf(map.getOrDefault(InteractionField.USER_ID.getFieldName(), null).toString()))
                .build();
    }
}
