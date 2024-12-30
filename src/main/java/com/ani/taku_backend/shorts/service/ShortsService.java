package com.ani.taku_backend.shorts.service;

import com.ani.taku_backend.shorts.domain.dto.ShortsCreateReqDTO;
import com.ani.taku_backend.shorts.domain.dto.res.ShortsResponseDTO;
import com.ani.taku_backend.user.model.entity.User;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public interface ShortsService {
    void createShort(ShortsCreateReqDTO createReqDTO, User user);
    ShortsResponseDTO findShortsInfo(String shortsId);

    void shortsLike(User user, String shortsId);
    void shortsDisLike(User user, String shortsId);

    default String generateUniqueFilePath(String originalFileName) {
        Objects.requireNonNull(originalFileName);

        String fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
        String date = LocalDate.now().toString();
        // 메인 폴더 이름
        String rootDirName = UUID.randomUUID().toString();
        String uniqueName = UUID.randomUUID().toString();
        return date + "/" + rootDirName + "/" + uniqueName + fileExtension;
    }

    // 오늘 날짜 / 루트 UUID 폴더 이름 반환
    default String getRootDirectoryPath(String fileUrl) {
        Objects.requireNonNull(fileUrl);

        String[] fileUrlSplit = fileUrl.split("/");
        return fileUrlSplit[0] + "/" + fileUrlSplit[1];
    }
}
