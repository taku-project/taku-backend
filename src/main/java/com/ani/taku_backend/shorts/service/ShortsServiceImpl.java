package com.ani.taku_backend.shorts.service;

import com.ani.taku_backend.common.service.FileService;
import com.ani.taku_backend.common.util.VideoConversionService;
import com.ani.taku_backend.shorts.domain.dto.ShortsFFmPegUrlResDTO;
import com.ani.taku_backend.shorts.domain.dto.ShortsCreateReqDTO;
import com.ani.taku_backend.shorts.domain.entity.Shorts;
import com.ani.taku_backend.shorts.repository.ShortsRepository;
import com.ani.taku_backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ShortsServiceImpl implements  ShortsService {
    private final ShortsRepository shortsRepository;
    private final VideoConversionService videoConversionService;
    private final UserRepository userRepository;
    private final MongoTemplate mongoTemplate;
    private final FileService fileService;
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
}
