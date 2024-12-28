package com.ani.taku_backend.shorts.service;

import com.ani.taku_backend.common.service.FileService;
import com.ani.taku_backend.common.util.VideoConversionService;
import com.ani.taku_backend.shorts.domain.dto.ShortsFFmPegUrlResDTO;
import com.ani.taku_backend.shorts.domain.dto.ShortsRecommendResDTO;
import com.ani.taku_backend.shorts.domain.dto.ShortsCreateReqDTO;
import com.ani.taku_backend.shorts.domain.entity.Shorts;
import com.ani.taku_backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.core.ParameterizedTypeReference;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShortsServiceImpl implements  ShortsService {
    // private final VideoConversionService videoConversionService;
    private final UserRepository userRepository;
    private final MongoTemplate mongoTemplate;
    private final FileService fileService;
    private final RestTemplate restTemplate;

    @Value("${flask.url}")
    private String flaskUrl;

    @Transactional
    @Override
    public void createShort(ShortsCreateReqDTO createReqDTO) {
        try {
            // 정보 등록 TODO user 정보 가져오기
            String uniqueFilePath = this.generateUniqueFilePath("userId", createReqDTO.getFile().getOriginalFilename());

            fileService.uploadFile(createReqDTO.getFile());
            // AWS Lambda 호출해 원본 파일 R2 저장 후 저장 url 반환
//            ShortsFFmPegUrlResDTO ffmpegUrlDTO = videoConversionService.processVideo("fileKey");

            Shorts shorts = Shorts.create(createReqDTO, uniqueFilePath);

            mongoTemplate.save(shorts);

        } catch (Exception e) {
            e.printStackTrace();
            // TODO
        }
    }


    @Override
    public List<ShortsRecommendResDTO> findRecommendShorts() {
        String restUrl = this.flaskUrl.replace("extract-keywords", "getMongo");

        Map<String, Integer> user = new HashMap<>();
        user.put("userId", 905);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<List<Shorts>> response = this.restTemplate.exchange(
            restUrl, 
            HttpMethod.POST,
            new HttpEntity<>(user, headers),
            new ParameterizedTypeReference<List<Shorts>>() {}
        );

        List<Shorts> shorts = response.getBody();
        log.info("추천된 쇼츠 목록: {}", shorts);

        List<ShortsRecommendResDTO> shortsRecommendResDTOs = shorts.stream().map(ShortsRecommendResDTO::of).collect(Collectors.toList());

        log.info("추천된 쇼츠 목록: {}", shortsRecommendResDTOs);
        return shortsRecommendResDTOs;
    }
}
