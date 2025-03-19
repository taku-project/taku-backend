package com.ani.taku_backend.admin.profanity.controller;

import com.ani.taku_backend.admin.profanity.dto.res.ProfannityResDTO;
import com.ani.taku_backend.admin.profanity.dto.req.CreateProfanityReqDTO;
import com.ani.taku_backend.admin.profanity.dto.req.SearchProfanityReqDTO;
import com.ani.taku_backend.admin.profanity.dto.req.UpdateProfanityReqDTO;
import com.ani.taku_backend.admin.profanity.dto.res.CreateProfanityResDTO;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.RequiredArgsConstructor;

import com.ani.taku_backend.admin.profanity.service.ProfanityFilterService;
import com.ani.taku_backend.common.response.CommonResponse;

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
     * @param createProfanityReqDTO
     * @return
     */
    @PostMapping("")
    public CommonResponse<CreateProfanityResDTO> createProfanityFilter(@RequestBody @Valid CreateProfanityReqDTO createProfanityReqDTO) {

        CreateProfanityResDTO profanityFilter = this.profanityFilterService.createProfanityFilter(null, createProfanityReqDTO);
        return CommonResponse.created(profanityFilter);
    }

    /**
     * 금칙어 필터 목록 조회
     * @param searchProfanityReqDTO
     * @param pageable
     * @return
     */
    @GetMapping("")
    public CommonResponse<Page<ProfannityResDTO>> getProfanityFilters(
        @ModelAttribute SearchProfanityReqDTO searchProfanityReqDTO,
        @PageableDefault(size = 20, sort = "keyword", direction = Sort.Direction.ASC) Pageable pageable

    ) {
        Page<ProfannityResDTO> profanityFilters = this.profanityFilterService.findProfanityFilterList(searchProfanityReqDTO, pageable);
        return CommonResponse.ok(profanityFilters);
    }

    /**
     * 금칙어 필터 삭제
     * @param id
     * @return
     */
    @DeleteMapping("/{id}")
    public CommonResponse<Void> deleteProfanityFilter(@PathVariable("id") Long id) {
        this.profanityFilterService.deleteProfanityFilter(id);
        return CommonResponse.ok(null);
    }

    /**
     * 금칙어 필터 수정
     * @param id
     * @param updateProfanityReqDTO
     * @return
     */
    @PutMapping("/{id}")
    public CommonResponse<Void> updateProfanityFilter(@PathVariable("id") Long id, @RequestBody @Valid UpdateProfanityReqDTO updateProfanityReqDTO) {
        this.profanityFilterService.updateProfanityFilter(id, updateProfanityReqDTO);
        return CommonResponse.ok(null);
    }

}
