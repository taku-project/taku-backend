package com.ani.taku_backend.admin.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.RequiredArgsConstructor;

import com.ani.taku_backend.admin.domain.dto.CreateProfanityRequestDTO;
import com.ani.taku_backend.admin.domain.dto.RequestSearchProfanityDTO;
import com.ani.taku_backend.admin.domain.dto.RequestUpdateProfanityDTO;
import com.ani.taku_backend.admin.domain.dto.ResponseCreateProfanityDTO;
import com.ani.taku_backend.admin.service.ProfanityFilterService;
import com.ani.taku_backend.common.response.ApiResponse;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PutMapping;

@RestController
@RequestMapping("/api/admin/profanity")
@RequiredArgsConstructor
public class ProfanityController {

    private final ProfanityFilterService profanityFilterService;


    /**
     * 금칙어 필터 생성
     * @param requestCreateProfanityDTO
     * @return
     */
    @PostMapping("")
    public ApiResponse<CreateProfanityRequestDTO> createProfanityFilter(@RequestBody @Valid CreateProfanityRequestDTO requestCreateProfanityDTO) {
        
        CreateProfanityRequestDTO profanityFilter = this.profanityFilterService.createProfanityFilter(null, requestCreateProfanityDTO);
        return ApiResponse.created(profanityFilter);
    }

    /**
     * 금칙어 필터 목록 조회
     * @param requestSearchProfanityDTO
     * @param pageable
     * @return
     */
    @GetMapping("")
    public ApiResponse<Page<ProfannityResponseDTO>> getProfanityFilters(
        @ModelAttribute RequestSearchProfanityDTO requestSearchProfanityDTO,
        @PageableDefault(size = 20, sort = "keyword", direction = Sort.Direction.ASC) Pageable pageable

    ) {
        Page<ProfannityResponseDTO> profanityFilters = this.profanityFilterService.findProfanityFilterList(requestSearchProfanityDTO, pageable);
        return ApiResponse.ok(profanityFilters);
    }

    /**
     * 금칙어 필터 삭제
     * @param id
     * @return
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteProfanityFilter(@PathVariable("id") Long id) {
        this.profanityFilterService.deleteProfanityFilter(id);
        return ApiResponse.ok(null);
    }

    /**
     * 금칙어 필터 수정
     * @param id
     * @param requestUpdateProfanityDTO
     * @return
     */
    @PutMapping("/{id}")
    public ApiResponse<Void> updateProfanityFilter(@PathVariable("id") Long id, @RequestBody @Valid RequestUpdateProfanityDTO requestUpdateProfanityDTO) {
        this.profanityFilterService.updateProfanityFilter(id, requestUpdateProfanityDTO);
        return ApiResponse.ok(null);
    }

}
