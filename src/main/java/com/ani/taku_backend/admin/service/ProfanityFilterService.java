package com.ani.taku_backend.admin.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.criteria.Predicate;

import java.time.Duration;
import java.util.ArrayList;

import com.ani.taku_backend.admin.domain.dto.ProfannityResponseDTO;
import com.ani.taku_backend.admin.domain.dto.RequestCreateProfanityDTO;
import com.ani.taku_backend.admin.domain.dto.RequestSearchProfanityDTO;
import com.ani.taku_backend.admin.domain.dto.RequestUpdateProfanityDTO;
import com.ani.taku_backend.admin.domain.dto.ResponseCreateProfanityDTO;
import com.ani.taku_backend.admin.domain.entity.ProfanityFilter;
import com.ani.taku_backend.admin.domain.repository.ProfanityFilterRepository;
import com.ani.taku_backend.common.annotation.RequireUser;
import com.ani.taku_backend.common.annotation.ValidateProfanity;
import com.ani.taku_backend.common.enums.StatusType;
import com.ani.taku_backend.common.exception.DuckwhoException;
import com.ani.taku_backend.common.exception.ErrorCode;
import com.ani.taku_backend.common.service.RedisService;
import com.ani.taku_backend.user.model.dto.PrincipalUser;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProfanityFilterService {

    private final ProfanityFilterRepository profanityFilterRepository;
    private final RedisService redisService;

    // 레디스 키
    private final String PROFANITY_FILTER_KEY = "profanity_filter_keywords";
    // 레디스 키 유효 시간
    private final Duration PROFANITY_FILTER_KEY_VALIDITY_TIME = Duration.ofDays(1);

    /**
     * 레디스에 저장된 금칙어 필터 키 목록 조회
     * @return
     */
    public List<String> getAllProfanityFilterKeywords() {

        // 레디스에 저장된 금칙어 필터 키 목록
        List<String> keyValues = null;
        try{
            keyValues = this.redisService.getKeyValues(PROFANITY_FILTER_KEY);
        } catch (Exception e) {
            log.error("금칙어 필터 키 조회 실패", e);
        }

        // 레디스에 저장된 금칙어 필터 키 목록이 없는 경우
        if (keyValues == null) {
            keyValues = this.refreshProfanityFilterKeywords();
        }
        return keyValues;
    }
    /**
     * 금칙어 필터 생성
     * @param principalUser
     * @param requestCreateProfanityDTO
     * @return
     */
    @RequireUser(isAdmin = true)
    // @ValidateProfanity(fields = {"keyword"})
    public ResponseCreateProfanityDTO createProfanityFilter(PrincipalUser principalUser, RequestCreateProfanityDTO requestCreateProfanityDTO) {

        // 이미 존재하는 금칙어 필터인지 확인 
        Optional<ProfanityFilter> existingProfanityFilter = this.profanityFilterRepository.findByKeyword(requestCreateProfanityDTO.getKeyword());
        if (existingProfanityFilter.isPresent()) {
            throw new DuckwhoException(ErrorCode.DUPLICATE_PROFANITY_FILTER);
        }

        ProfanityFilter save = this.profanityFilterRepository.save(
            ProfanityFilter.builder()
                .admin(principalUser.getUser())
                .keyword(requestCreateProfanityDTO.getKeyword())
                .explaination(requestCreateProfanityDTO.getExplaination())
                .status(StatusType.ACTIVE)
                .build()
        );

        // 레디스에 저장된 금칙어 필터 키 목록 갱신
        this.refreshProfanityFilterKeywords();

        return ResponseCreateProfanityDTO.of(save);
    }

    /**
     * 금칙어 필터 목록 조회
     * @param requestSearchProfanityDTO
     * @param pageable
     * @return
     */
    @RequireUser(isAdmin = true)
    public Page<ProfannityResponseDTO> findProfanityFilterList(RequestSearchProfanityDTO requestSearchProfanityDTO,
            Pageable pageable) {

        Specification<ProfanityFilter> specification = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (requestSearchProfanityDTO.getUserName() != null) {
                predicates.add(criteriaBuilder.like(root.get("admin").get("nickname"), 
                    "%" + requestSearchProfanityDTO.getUserName() + "%"));
            }
            if (requestSearchProfanityDTO.getKeyword() != null) {
                predicates.add(criteriaBuilder.like(root.get("keyword"), 
                    "%" + requestSearchProfanityDTO.getKeyword() + "%"));
            }
            if (requestSearchProfanityDTO.getExplaination() != null) {
                predicates.add(criteriaBuilder.like(root.get("explaination"), 
                    "%" + requestSearchProfanityDTO.getExplaination() + "%"));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        return profanityFilterRepository.findAll(specification, pageable)
                .map(ProfannityResponseDTO::of);
    }

    /**
     * 금칙어 필터 삭제
     * @param id
     */
    @RequireUser(isAdmin = true)
    public void deleteProfanityFilter(Long id) {
        // 금칙어 필터 조회
        Optional<ProfanityFilter> profanityFilter = this.profanityFilterRepository.findById(id);
        if (profanityFilter.isEmpty()) {
            throw new DuckwhoException(ErrorCode.NOT_FOUND_PROFANITY_FILTER);
        }
        // 금칙어 필터 삭제
        this.profanityFilterRepository.delete(profanityFilter.get());
    }

    /**
     * 금칙어 필터 수정
     * @param id
     * @param requestUpdateProfanityDTO
     */
    @RequireUser(isAdmin = true)
    @Transactional
    @ValidateProfanity(fields = {"keyword"})
    public void updateProfanityFilter(Long id, RequestUpdateProfanityDTO requestUpdateProfanityDTO) {

        ProfanityFilter profanityEntity = this.profanityFilterRepository.findById(id).orElseThrow(() -> new DuckwhoException(ErrorCode.NOT_FOUND_PROFANITY_FILTER));

        profanityEntity.update(requestUpdateProfanityDTO);

        // 레디스에 저장된 금칙어 필터 키 목록 갱신
        this.refreshProfanityFilterKeywords();
    }

    /**
     * 금칙어 필터 키 목록 갱신
     * @return
     */
    private List<String> refreshProfanityFilterKeywords() {
        // 레디스에 저장된 금칙어 필터 키 목록 조회

        // DB에서 금칙어 필터 목록 조회
        List<ProfanityFilter> profanityFilters = this.profanityFilterRepository.findAll();
        List<String> keyValues = profanityFilters.stream()
            .filter(profanityFilter -> profanityFilter.getStatus().equals(StatusType.ACTIVE))
            .map(profanityFilter -> profanityFilter.getKeyword().toLowerCase().replace(" ", ""))
            .collect(Collectors.toList());

        // 레디스에 저장
        this.redisService.setKeyValues(PROFANITY_FILTER_KEY, keyValues, PROFANITY_FILTER_KEY_VALIDITY_TIME);
        return keyValues;
    }
}

