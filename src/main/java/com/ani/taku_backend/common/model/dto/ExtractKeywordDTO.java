package com.ani.taku_backend.common.model.dto;

import java.util.List;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class ExtractKeywordDTO {

    private String text;
    private List<String> keywords;
    
}