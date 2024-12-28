package com.ani.taku_backend.shorts.service;

import com.ani.taku_backend.shorts.domain.dto.ShortsCreateReqDTO;
import com.ani.taku_backend.shorts.domain.dto.ShortsRecommendResDTO;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public interface ShortsService {
    void createShort(ShortsCreateReqDTO createReqDTO);

    List<ShortsRecommendResDTO> findRecommendShorts();

    default String generateUniqueFilePath(String userId, String originalFileName) {
        Objects.requireNonNull(userId);
        Objects.requireNonNull(originalFileName);

        String fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
        String date = LocalDate.now().toString(); // 예: 2024-12-24
        String uniqueName = UUID.randomUUID().toString();
        return "/" + date + "/" + userId + "/" + uniqueName + fileExtension;
    }
}
