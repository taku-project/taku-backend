package com.ani.taku_backend.user_jangter.service;

import com.ani.taku_backend.user_jangter.dto.UserPurchaseResponseDTO;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

public interface UserJangterService {
    PageImpl<UserPurchaseResponseDTO> findUserPurchaseList(Long userId, Pageable pageable);
}
