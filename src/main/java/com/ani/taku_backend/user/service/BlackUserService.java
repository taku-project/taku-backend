package com.ani.taku_backend.user.service;

import java.util.List;
import org.springframework.stereotype.Service;
import com.ani.taku_backend.user.model.entity.BlackUser;
import com.ani.taku_backend.user.repository.BlackUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class BlackUserService {


    private final BlackUserRepository blackUserRepository;

    public List<BlackUser> findByUserId(Long userId) {
        return blackUserRepository.findByUser_Id(userId);
    }
    
}
