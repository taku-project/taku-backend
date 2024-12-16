package com.ani.taku_backend.global.response;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ListResponse <T> extends CommonResponse{
    List<T> dataList;
}
