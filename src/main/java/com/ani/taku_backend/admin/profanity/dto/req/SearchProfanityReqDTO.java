package com.ani.taku_backend.admin.profanity.dto.req;

import lombok.Data;

@Data
public class SearchProfanityReqDTO {

    private String userName;
    private String keyword;
    private String explaination;

}
