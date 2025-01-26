package com.ani.taku_backend.user_jangter.repository;

import com.ani.taku_backend.user_jangter.dto.UserPurchaseResponseDTO;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

public interface UserJangterRepository {
    // 유저 구매 목록
    PageImpl<UserPurchaseResponseDTO> findUserPurchaseList(Long userId, Pageable pageable);
}