package com.ani.taku_backend.shorts_interaction.domain.dto.req;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Duration;

@Schema(name = "쇼츠 재생 기록 요청 객체", description = "쇼츠 재생 기록 생성에 필요한 객체.")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateShortsViewReqDTO {
    @Schema(name = "viewTime", description = "사용자가 쇼츠를 시청한 시간. 60초 짜리 동영상을 모두 보고 추가로 10초를 보면 시청 시간은 70초가 됨.", example = "PT1M30S")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Duration viewTime;
    @Schema(name = "playTime", description = "해당 쇼츠의 총 재생 시간.", example = "PT30S")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Duration playTime;
}
