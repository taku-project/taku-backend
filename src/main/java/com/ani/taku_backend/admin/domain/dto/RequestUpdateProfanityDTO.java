package com.ani.taku_backend.admin.domain.dto;

import com.ani.taku_backend.common.enums.StatusType;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import jakarta.validation.constraints.Pattern;

@Data
public class RequestUpdateProfanityDTO {

    @Pattern(regexp = "^(?!\\s*$).+", message = "키워드는 빈 값일 수 없습니다")
    private String keyword;

    @Pattern(regexp = "^(?!\\s*$).+", message = "설명은 빈 값일 수 없습니다")
    private String explaination;

    @NotNull(message = "상태는 필수 입력 항목입니다.")
    private StatusType status;
}
