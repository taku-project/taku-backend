package com.ani.taku_backend.user_jangter.service;

import com.ani.taku_backend.user_jangter.dto.UserPurchaseResponseDTO;
import com.ani.taku_backend.user_jangter.repository.UserJangterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserJangterServiceImpl implements UserJangterService {
    private final UserJangterRepository userJangterRepository;

    @Override
    public PageImpl<UserPurchaseResponseDTO> findUserPurchaseList(Long userId, Pageable pageable) {
        return userJangterRepository.findUserPurchaseList(userId, pageable);
    }
}
