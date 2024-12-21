package com.ani.taku_backend.admin.service;

import org.springframework.stereotype.Service;

import com.ani.taku_backend.admin.domain.repository.ProfanityFilterRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class ForbiddenKeywordService {

    private final ProfanityFilterRepository profanityFilterRepository;

    
    
}

