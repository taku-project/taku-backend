package com.ani.taku_backend.common.service;

import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.ani.taku_backend.common.model.dto.ExtractKeywordDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExtractKeywordService {

    private final RestTemplate restTemplate;

    @Value("${flask.url}")
    private String flaskUrl;

    public List<String> extractKeywords(String text) {

        if(text == null || text.isEmpty()) {
            log.error("Text is null or empty");
            return null;
        }

        HashMap<String, String> request = new HashMap<>();
        request.put("text", text);

        ResponseEntity<ExtractKeywordDTO> response = this.restTemplate.postForEntity(this.flaskUrl, request, ExtractKeywordDTO.class);

        if(response.getStatusCode() != HttpStatus.OK) {
            log.error("Failed to extract keywords");
            return null;
        }

        return response.getBody().getKeywords();
    }
}


