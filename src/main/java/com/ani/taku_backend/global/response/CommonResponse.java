package com.ani.taku_backend.global.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommonResponse {
    boolean success;
    int code;
    String message;

}
