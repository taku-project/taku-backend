package com.ani.taku_backend.shorts.service;

import com.ani.taku_backend.common.annotation.RequireUser;
import com.ani.taku_backend.common.exception.ErrorCode;
import com.ani.taku_backend.common.exception.UserException;
import com.ani.taku_backend.common.service.FileService;
import com.ani.taku_backend.common.util.VideoConversionService;
import com.ani.taku_backend.shorts.domain.dto.ShortsCommentDTO;
import com.ani.taku_backend.shorts.domain.dto.ShortsCreateReqDTO;
import com.ani.taku_backend.shorts.domain.dto.ShortsFFmPegUrlResDTO;
import com.ani.taku_backend.shorts.domain.dto.ShortsInfoResDTO;
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
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.core.ParameterizedTypeReference;

import java.util.Objects;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    @Value("${flask.find-shorts-comment-url}")
    private String findShortsCommentUrl;

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
    public List<ShortsInfoResDTO> findRecommendShorts(PrincipalUser principalUser) {
        
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

        List<ShortsInfoResDTO> shortsInfoResDTOs = shorts.stream().map(ShortsInfoResDTO::of).collect(Collectors.toList());

        log.info("추천된 쇼츠 목록: {}", shortsInfoResDTOs);
        return shortsInfoResDTOs;
    }

    @Override
    public List<ShortsCommentDTO> findShortsComment(String shortsId) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(this.findShortsCommentUrl)
            .queryParam("shorts_id", shortsId);
            
        String url = builder.toUriString();
        log.info("쇼츠 댓글 조회 요청: {}", url);

        // Header 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<List<ShortsCommentDTO>> response = this.restTemplate.exchange(
            url,
            HttpMethod.GET,
            new HttpEntity<>(null, headers),
            new ParameterizedTypeReference<List<ShortsCommentDTO>>() {}
        );

        List<ShortsCommentDTO> shortsCommentDTOs = response.getBody();
        log.info("쇼츠 댓글 목록: {}", shortsCommentDTOs);

        if(!shortsCommentDTOs.isEmpty()) {
            this.setCommentUser(shortsCommentDTOs);
        }
        return shortsCommentDTOs;
    }

    /**
     * 댓글 작성자 정보 설정
     * @param shortsCommentDTOs
     */
    private void setCommentUser(List<ShortsCommentDTO> shortsCommentDTOs) {
        // 댓글 작성자들 아이디 목록 조회
        List<Long> userIds = shortsCommentDTOs.stream()
            .map(comment -> comment.getUserInfo().getId())
            .distinct()
            .collect(Collectors.toList());
        log.info("댓글 작성자들 아이디 목록: {}", userIds);

        // 댓글 작성자들 정보 조회
        List<User> users = this.userRepository.findByUserIdIn(userIds);

        List<ShortsCommentDTO.CommentUserDTO> commentUserDTOs = users.stream()
            .map(ShortsCommentDTO.CommentUserDTO::of)
            .collect(Collectors.toList());

        // 댓글 작성자들 정보 설정
        shortsCommentDTOs.forEach(comment -> {
            comment.setUserInfo(commentUserDTOs.stream()
                .filter(user -> user.getId().equals(comment.getUserInfo().getId()))
                .findFirst()
                .orElse(null));
        });
    }
}