package com.ani.taku_backend.admin.profanity.dto.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateProfanityReqDTO {

    @NotBlank
    private String keyword;

    @NotBlank
    private String explaination;
}

