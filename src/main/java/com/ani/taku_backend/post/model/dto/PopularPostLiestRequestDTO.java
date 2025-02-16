package com.ani.taku_backend.post.model.dto;

import com.ani.taku_backend.post.model.enums.PopularPeriodType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Schema(description = "인기글 목록 조회 반환 객체")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class PopularPostLiestRequestDTO {
    private List<PopularPostItemDTO> popularPosts;
    @Schema(description = "인기글 조회 기간")
    private PopularPeriodType period;
}
