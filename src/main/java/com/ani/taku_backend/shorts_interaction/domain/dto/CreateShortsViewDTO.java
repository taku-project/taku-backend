package com.ani.taku_backend.shorts_interaction.domain.dto;

import com.ani.taku_backend.user.model.entity.User;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Duration;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class CreateShortsViewDTO {
    private String shortsId;
    private User user;
    private Duration viewDuration;
    private Duration playDuration;
}
