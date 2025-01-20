package com.ani.taku_backend.user_jangter.repository;

import com.ani.taku_backend.user_jangter.dto.res.UserCellResponseDTO;
import com.ani.taku_backend.user_jangter.dto.res.UserPurchaseResponseDTO;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

public interface UserJangterRepository {
    // 유저 구매 목록
    PageImpl<UserPurchaseResponseDTO> findUserPurchaseList(Long userId, Pageable pageable);

    PageImpl<UserCellResponseDTO> findUserCellList(Long userId, Pageable pageable);
}