package com.ani.taku_backend.global.response;

import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ResponseService {

    public <T> SingleResponse<T> getSingleResponse(T data) {
        SingleResponse<T> response = new SingleResponse<>();
        response.setData(data);
        setSuccessResponse(response);
        return response;
    }

    public <T> ListResponse<T> getListResponse(List<T> dataList) {
        ListResponse<T> response = new ListResponse<>();
        response.setDataList(dataList);
        setSuccessResponse(response);
        return response;
    }

    public CommonResponse getErrorResponse(int code, String message){
        CommonResponse response = new CommonResponse();
        response.setSuccess(false);
        response.setCode(code);
        response.setMessage(message);
        return response;
    }

    private void setSuccessResponse(CommonResponse response) {
        response.setCode(0);
        response.setSuccess(true);
        response.setMessage("SUCCESS");
    }


}