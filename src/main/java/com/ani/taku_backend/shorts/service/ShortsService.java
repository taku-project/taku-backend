package com.ani.taku_backend.shorts.service;

import com.ani.taku_backend.shorts.domain.dto.ShortsCommentCreateReqDTO;
import com.ani.taku_backend.shorts.domain.dto.ShortsCommentDTO;
import com.ani.taku_backend.shorts.domain.dto.ShortsCommentUpdateReqDTO;
import com.ani.taku_backend.shorts.domain.dto.ShortsCreateReqDTO;
import com.ani.taku_backend.shorts.domain.dto.ShortsInfoResDTO;
import com.ani.taku_backend.user.model.dto.PrincipalUser;
import com.ani.taku_backend.shorts.domain.dto.res.ShortsResponseDTO;
import com.ani.taku_backend.user.model.entity.User;

import jakarta.validation.Valid;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public interface ShortsService {
    void createShort(ShortsCreateReqDTO createReqDTO, User user);

    List<ShortsInfoResDTO> findRecommendShorts(PrincipalUser principalUser);
    List<ShortsCommentDTO> findShortsComment(String shortsId);
    void createShortsComment(PrincipalUser principalUser, ShortsCommentCreateReqDTO shortsCommentCreateReqDTO, String shortsId);
    void updateShortsComment(PrincipalUser principalUser, ShortsCommentUpdateReqDTO shortsCommentUpdateReqDTO, String commentId);
    void deleteShortsComment(PrincipalUser principalUser, String commentId);
    void createShortsReply(PrincipalUser principalUser, ShortsCommentCreateReqDTO shortsCommentCreateReqDTO, String commentId);
    void deleteShortsReply(PrincipalUser principalUser, String commentId, String replyId);
    void updateShortsReply(PrincipalUser principalUser, ShortsCommentUpdateReqDTO shortsCommentUpdateReqDTO, String commentId, String replyId);
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
