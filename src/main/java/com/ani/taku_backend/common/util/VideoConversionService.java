package com.ani.taku_backend.common.util;

import com.ani.taku_backend.shorts.domain.dto.ShortsFFmPegUrlResDTO;

public interface VideoConversionService {
    ShortsFFmPegUrlResDTO processVideo(String fileKey);
}
