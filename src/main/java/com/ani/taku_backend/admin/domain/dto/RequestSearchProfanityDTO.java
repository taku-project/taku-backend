package com.ani.taku_backend.admin.domain.dto;

import lombok.Data;

@Data
public class RequestSearchProfanityDTO {

    private String userName;
    private String keyword;
    private String explaination;

}
