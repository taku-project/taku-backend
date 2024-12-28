package com.ani.taku_backend.shorts.service;

import com.ani.taku_backend.common.exception.ErrorCode;
import com.ani.taku_backend.common.exception.UserException;
import com.ani.taku_backend.common.service.FileService;
import com.ani.taku_backend.common.util.VideoConversionService;
import com.ani.taku_backend.shorts.domain.dto.ShortsCreateReqDTO;
import com.ani.taku_backend.shorts.domain.dto.ShortsFFmPegUrlResDTO;
import com.ani.taku_backend.shorts.domain.entity.Shorts;
import com.ani.taku_backend.user.model.entity.User;
import com.ani.taku_backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ShortsServiceImpl implements  ShortsService {
    private final VideoConversionService videoConversionService;
    private final UserRepository userRepository;
    private final MongoTemplate mongoTemplate;
    private final FileService fileService;
    @Transactional
    @Override
    public void createShort(ShortsCreateReqDTO createReqDTO, User user) {
        Objects.requireNonNull(user);
        String uniqueFilePath = this.generateUniqueFilePath(createReqDTO.getFile().getOriginalFilename());
        Shorts shorts = null;
        try {
            User uploader = userRepository.findById(user.getUserId())
                    .orElseThrow(UserException.UserNotFoundException::new);
            if("INACTIVE".equals(uploader.getStatus())) {
                throw new UserException(ErrorCode.USER_NOT_FOUND.getMessage());
            }

            String fileUrl = fileService.uploadFile(createReqDTO.getFile(), uniqueFilePath);
            // AWS Lambda 호출해 원본 파일 R2 저장 후 저장된 파일 객체 반환
            ShortsFFmPegUrlResDTO ffmpegUrlDTO = videoConversionService.ffmpegConversion(fileUrl);

            shorts = Shorts.create(uploader, createReqDTO, uniqueFilePath, ffmpegUrlDTO);

            mongoTemplate.save(shorts);
        } catch (Exception e) {
            String rootDirPath = this.getRootDirectoryPath(uniqueFilePath);
            fileService.deleteFolder(rootDirPath);

            if(shorts != null) {
                mongoTemplate.remove(shorts);
            }
            e.printStackTrace();
        }
    }
}
