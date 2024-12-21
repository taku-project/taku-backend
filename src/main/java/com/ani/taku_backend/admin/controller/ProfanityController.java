package com.ani.taku_backend.admin.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.RequiredArgsConstructor;

import com.ani.taku_backend.admin.domain.dto.RequestCreateProfanityDTO;
import com.ani.taku_backend.admin.domain.dto.ResponseCreateProfanityDTO;
import com.ani.taku_backend.admin.service.ProfanityFilterService;
import com.ani.taku_backend.global.response.ApiResponse;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import jakarta.validation.Valid;
@RestController
@RequestMapping("/admin/api/profanity")
@RequiredArgsConstructor
public class ProfanityController {

    private final ProfanityFilterService profanityFilterService;


    /**
     * 금칙어 필터 생성
     * @param requestCreateProfanityDTO
     * @return
     */
    @PostMapping("")
    public ApiResponse<ResponseCreateProfanityDTO> createProfanityFilter(@RequestBody @Valid RequestCreateProfanityDTO requestCreateProfanityDTO) {
        
        ResponseCreateProfanityDTO profanityFilter = this.profanityFilterService.createProfanityFilter(null, requestCreateProfanityDTO);
        return ApiResponse.created(profanityFilter);
    }
}
