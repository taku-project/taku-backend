package com.ani.taku_backend.common.util;

import com.ani.taku_backend.shorts.domain.dto.ShortsFFmPegUrlResDTO;
import com.nimbusds.jose.shaded.gson.Gson;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.InvokeRequest;
import software.amazon.awssdk.services.lambda.model.InvokeResponse;

import java.nio.charset.Charset;

// 파일 변환 서비스 Aws Lambda 구현체
@Service
public class AwsLambdaVideoConversionService implements VideoConversionService {
    private final LambdaClient lambdaClient;

    AwsLambdaVideoConversionService() {
        this.lambdaClient = LambdaClient.create();
    }
    private static final String LAMBDA_FUNC_NAME = "r2_file_upload";

    @Override
    public ShortsFFmPegUrlResDTO processVideo(String fileUrl) {

        try {
            InvokeRequest request = InvokeRequest.builder()
                    .functionName(LAMBDA_FUNC_NAME)
                    .payload(SdkBytes.fromString("String", Charset.defaultCharset()))
                    .build();
            // TODO AWS Lambda를 사용해 받아온 값
            InvokeResponse response = lambdaClient.invoke(request);
            return new Gson().fromJson(response.payload().asUtf8String(), ShortsFFmPegUrlResDTO.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
