package com.ani.taku_backend.shorts.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LambdaFileConversionResDTO {
    private int statusCode;
    private ShortsFFmPegUrlResDTO body;
}
