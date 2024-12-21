package com.ani.taku_backend.admin.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.ani.taku_backend.admin.domain.dto.RequestCreateProfanityDTO;
import com.ani.taku_backend.admin.domain.dto.ResponseCreateProfanityDTO;
import com.ani.taku_backend.admin.domain.entity.ProfanityFilter;
import com.ani.taku_backend.admin.domain.repository.ProfanityFilterRepository;
import com.ani.taku_backend.common.annotation.RequireUser;
import com.ani.taku_backend.common.enums.StatusType;
import com.ani.taku_backend.global.exception.CustomException;
import com.ani.taku_backend.global.exception.ErrorCode;
import com.ani.taku_backend.user.model.dto.PrincipalUser;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProfanityFilterService {

    private final ProfanityFilterRepository profanityFilterRepository;


    /**
     * 금칙어 필터 생성
     * @param principalUser
     * @param requestCreateProfanityDTO
     * @return
     */
    @RequireUser(isAdmin = true)
    public ResponseCreateProfanityDTO createProfanityFilter(PrincipalUser principalUser, RequestCreateProfanityDTO requestCreateProfanityDTO) {

        // 이미 존재하는 금칙어 필터인지 확인 
        Optional<ProfanityFilter> existingProfanityFilter = this.profanityFilterRepository.findByKeyword(requestCreateProfanityDTO.getKeyword());
        if (existingProfanityFilter.isPresent()) {
            throw new CustomException(ErrorCode.DUPLICATE_PROFANITY_FILTER);
        }

        ProfanityFilter save = this.profanityFilterRepository.save(
            ProfanityFilter.builder()
                .admin(principalUser.getUser())
                .keyword(requestCreateProfanityDTO.getKeyword())
                .explaination(requestCreateProfanityDTO.getExplaination())
                .status(StatusType.ACTIVE)
                .build()
        );
        return ResponseCreateProfanityDTO.of(save);
    }
    
}

