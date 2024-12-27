package com.ani.taku_backend.shorts.domain.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ShortsFFmPegUrlResDTO {
    private int status;
    private String m3u8Url;
    private List<String> segmentUrls;
    private int duration;
}
