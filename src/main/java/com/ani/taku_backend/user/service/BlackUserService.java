package com.ani.taku_backend.user.service;

import java.util.List;

import com.ani.taku_backend.common.exception.DuckwhoException;
import com.ani.taku_backend.user.model.dto.PrincipalUser;
import com.ani.taku_backend.user.model.entity.User;
import org.springframework.stereotype.Service;
import com.ani.taku_backend.user.model.entity.BlackUser;
import com.ani.taku_backend.user.repository.BlackUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static com.ani.taku_backend.common.exception.ErrorCode.UNAUTHORIZED_ACCESS;

@Service
@RequiredArgsConstructor
@Slf4j
public class BlackUserService {


    private final BlackUserRepository blackUserRepository;

    public List<BlackUser> findByUserId(Long userId) {
        return blackUserRepository.findByUser_UserId(userId);
    }

    // 블랙리스트 검증
    public User validateBlockUser(PrincipalUser principalUser) {
        User user = principalUser.getUser();
        List<BlackUser> byUserId = findByUserId(user.getUserId());
        if (!byUserId.isEmpty() && byUserId.get(0).getId().equals(user.getUserId())) {
            log.info("블랙유저 {}", user);
            throw new DuckwhoException(UNAUTHORIZED_ACCESS);
        }
        log.info("일반 유저 {}", user);
        return user;
    }
}
