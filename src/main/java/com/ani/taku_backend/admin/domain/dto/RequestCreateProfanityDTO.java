package com.ani.taku_backend.admin.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RequestCreateProfanityDTO {

    @NotBlank
    private String keyword;

    @NotBlank
    private String explaination;
}

