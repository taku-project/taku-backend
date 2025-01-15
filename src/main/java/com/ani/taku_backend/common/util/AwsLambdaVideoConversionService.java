package com.ani.taku_backend.common.util;

import com.ani.taku_backend.common.exception.DuckwhoException;
import com.ani.taku_backend.common.exception.ErrorCode;
import com.ani.taku_backend.shorts.domain.dto.LambdaFileConversionResDTO;
import com.ani.taku_backend.shorts.domain.dto.ShortsFFmPegUrlResDTO;
import com.nimbusds.jose.shaded.gson.Gson;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.InvokeRequest;
import software.amazon.awssdk.services.lambda.model.InvokeResponse;

import java.util.Map;

// 파일 변환 서비스 Aws Lambda 구현체
@Service
public class AwsLambdaVideoConversionService implements VideoConversionService {
    private final LambdaClient lambdaClient;

    AwsLambdaVideoConversionService(
            @Value("${aws.access_key}") String accessKeyId,
            @Value("${aws.secret_key}") String secretAccessKey,
            @Value("${aws.region}") String region
    ) {
        AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(accessKeyId, secretAccessKey);
        this.lambdaClient = LambdaClient.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                .build();
    }
    private static final String LAMBDA_FUNC_NAME = "r2_file_upload";

    @Override
    public ShortsFFmPegUrlResDTO ffmpegConversion(String fileUrl) {

        try {
            String payload = new Gson().toJson(Map.of("file_url", fileUrl));
            InvokeRequest request = InvokeRequest.builder()
                    .functionName(LAMBDA_FUNC_NAME)
                    .payload(SdkBytes.fromUtf8String(payload))
                    .build();

            InvokeResponse response = lambdaClient.invoke(request);
            String responsePayload = response.payload().asUtf8String();
            LambdaFileConversionResDTO responseDTO = new Gson().fromJson(responsePayload, LambdaFileConversionResDTO.class);

            if(responseDTO.getStatusCode() != 200) {
                throw new Exception(responseDTO.getBody().getMessage());
            }
            return responseDTO.getBody();
        } catch (Exception e) {
            e.printStackTrace();
            throw new DuckwhoException(ErrorCode.FILE_UPLOAD_ERROR);
        }
    }
}
