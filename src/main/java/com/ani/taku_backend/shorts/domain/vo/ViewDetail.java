package com.ani.taku_backend.shorts.domain.vo;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.Duration;

/**
 * 쇼츠 조회 상세 정보 (조회 시간, 조회 비율)
 */
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@ToString
public class ViewDetail implements InteractionDetail {
    private Duration playDuration;
    private Duration viewDuration;
    private Double viewRatio;
}
