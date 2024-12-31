package com.ani.taku_backend.shorts.service;

import com.ani.taku_backend.common.annotation.RequireUser;
import com.ani.taku_backend.common.exception.ErrorCode;
import com.ani.taku_backend.common.exception.FileException;
import com.ani.taku_backend.common.exception.UserException;
import com.ani.taku_backend.common.service.FileService;
import com.ani.taku_backend.common.util.VideoConversionService;
import com.ani.taku_backend.shorts.domain.dto.ShortsCreateReqDTO;
import com.ani.taku_backend.shorts.domain.dto.ShortsFFmPegUrlResDTO;
import com.ani.taku_backend.shorts.domain.dto.ShortsRecommendResDTO;
import com.ani.taku_backend.shorts.domain.dto.res.PopularityMaticResDTO;
import com.ani.taku_backend.shorts.domain.dto.res.ShortsResponseDTO;
import com.ani.taku_backend.shorts.domain.entity.Shorts;
import com.ani.taku_backend.user.model.dto.PrincipalUser;
import com.ani.taku_backend.user.model.entity.User;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.core.ParameterizedTypeReference;

import java.util.List;
import java.util.Objects;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShortsServiceImpl implements  ShortsService {
    private final VideoConversionService videoConversionService;
    private final UserRepository userRepository;
    private final MongoTemplate mongoTemplate;
    private final FileService fileService;
    private final RestTemplate restTemplate;

    @Value("${flask.recommend-shorts-url}")
    private String recommendShortsUrl;

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
            // TODO
        }
    }


    @Override
    public List<ShortsRecommendResDTO> findRecommendShorts(PrincipalUser principalUser) {
        
        // 유저 아이디 조회
        Long userId = 0L;
        if(principalUser != null) {
            userId = principalUser.getUser().getUserId();
        }
        Map<String, Long> user = new HashMap<>();
        user.put("user_id", userId);

        // Header 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<List<Shorts>> response = this.restTemplate.exchange(
            this.recommendShortsUrl,
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
    @Override
    public ShortsResponseDTO findShortsInfo(String shortsId) {
        Shorts shorts = Optional.ofNullable(mongoTemplate.findById(shortsId, Shorts.class))
                            .orElseThrow(FileException.FileNotFoundException::new);

        if(shorts.getFileInfo() != null) {
            Shorts.VideoMetadata fileInfo = shorts.getFileInfo();
            String m3u8Url = fileInfo.getRemoteStorageUrl()
                    .stream()
                    .filter(fileUrl -> fileUrl.endsWith(".m3u8"))
                    .findFirst()
                    .orElseThrow(FileException.FileNotFoundException::new);

            Shorts.PopularityMatic popularityMatics = shorts.getPopularityMatics();
            // TODO 사용자 상호 작용 테이블에서 유저가 like, dislike 했는지 확인
            return ShortsResponseDTO.builder()
                    .userProfileImg(shorts.getProfileImg())
                    .description(shorts.getDescription())
                    .popularityMatic(new PopularityMaticResDTO(popularityMatics))
                    .m3u8Url(m3u8Url)
                    .build();
        } else {
            throw new FileException.FileNotFoundException();
        }

    }

    @Transactional
    @Override
    public void shortsLike(User user, String shortsId) {
        // TODO 사용자 상호 작용 Document에서 dislike 존재 여부 확인
        // TODO 사용자 상호 작용 Document에서 like 존재 여부 확인
        // TODO dislike가 존재하면 like로 바꿈
        Shorts shorts = Optional.ofNullable(mongoTemplate.findById(shortsId, Shorts.class))
                .orElseThrow(FileException.FileNotFoundException::new);
        shorts.addLike();

        // TODO 사용자 상호 작용 Document에 생성
        mongoTemplate.save(shorts);
    }

    @Transactional
    @Override
    public void shortsDisLike(User user, String shortsId) {
        // TODO 사용자 상호 작용 Document에서 like 존재 여부 확인
        // TODO 사용자 상호 작용 Document에서 dislike 존재 여부 확인
        // TODO like가 존재하면 like 1 차감 후 dislike 1증가
        Shorts shorts = Optional.ofNullable(mongoTemplate.findById(shortsId, Shorts.class))
                .orElseThrow(FileException.FileNotFoundException::new);
        shorts.addLike();

        // TODO 사용자 상호 작용 Document에 생성
        mongoTemplate.save(shorts);
    }
}
