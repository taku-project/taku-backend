package com.ani.taku_backend.shorts.service;

import com.ani.taku_backend.common.annotation.RequireUser;
import com.ani.taku_backend.common.enums.InteractionType;
import com.ani.taku_backend.common.exception.DuckwhoException;
import com.ani.taku_backend.common.exception.ErrorCode;
import com.ani.taku_backend.common.exception.FileException;
import com.ani.taku_backend.common.exception.UserException;
import com.ani.taku_backend.common.service.FileService;
import com.ani.taku_backend.common.util.ObjectIdUtil;
import com.ani.taku_backend.common.util.VideoConversionService;
import com.ani.taku_backend.shorts.domain.dto.ShortsCommentCreateReqDTO;
import com.ani.taku_backend.shorts.domain.dto.ShortsCommentDTO;
import com.ani.taku_backend.shorts.domain.dto.ShortsCommentUpdateReqDTO;
import com.ani.taku_backend.shorts.domain.dto.ShortsCreateReqDTO;
import com.ani.taku_backend.shorts.domain.dto.ShortsFFmPegUrlResDTO;
import com.ani.taku_backend.shorts.domain.dto.ShortsInfoResDTO;
import com.ani.taku_backend.shorts.domain.dto.res.ShortsLikeInteractionResponseDTO;
import com.ani.taku_backend.shorts.domain.entity.Interaction;
import com.ani.taku_backend.shorts.domain.dto.res.PopularityMaticResDTO;
import com.ani.taku_backend.shorts.domain.dto.res.ShortsResponseDTO;
import com.ani.taku_backend.shorts.domain.entity.Shorts;
import com.ani.taku_backend.shorts.domain.vo.CommentDetail;
import com.ani.taku_backend.shorts_interaction.repository.InteractionRepository;
import com.ani.taku_backend.user.model.dto.PrincipalUser;
import com.ani.taku_backend.shorts.repository.ShortsRepository;
import com.ani.taku_backend.user.model.entity.User;
import com.ani.taku_backend.user.repository.UserRepository;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
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

import java.util.List;
import java.util.Objects;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;


import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShortsServiceImpl implements  ShortsService {
    private final VideoConversionService videoConversionService;
    private final UserRepository userRepository;
    private final ShortsRepository shortsRepository;
    private final InteractionRepository interactionRepository;
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

            String fileUrl = fileService.uploadVideoFile(createReqDTO.getFile(), uniqueFilePath);
            // AWS Lambda 호출해 원본 파일 R2 저장 후 저장된 파일 객체 반환
            ShortsFFmPegUrlResDTO ffmpegUrlDTO = videoConversionService.ffmpegConversion(fileUrl);

            shorts = Shorts.create(uploader, createReqDTO, uniqueFilePath, ffmpegUrlDTO);

            shortsRepository.save(shorts);
        } catch (Exception e) {
            String rootDirPath = this.getRootDirectoryPath(uniqueFilePath);
            fileService.deleteFolder(rootDirPath);

            if(shorts != null) {
                shortsRepository.delete(shorts);
            }
            e.printStackTrace();
        }
    }


    /**
     * 쇼츠 추천 조회
     * @param principalUser
     * @return
     */
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

    /**
     * 쇼츠 댓글 조회
     * @param shortsId
     * @return
     */
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


    /**
     * 쇼츠 댓글 생성
     * @param principalUser
     * @param shortsCommentCreateReqDTO
     * @return
     */

    @Transactional
    @RequireUser
    @Override
    public void createShortsComment(PrincipalUser principalUser,
                                    ShortsCommentCreateReqDTO shortsCommentCreateReqDTO,
                                    String shortsId) {

        User user = principalUser.getUser();

        // 변환
        Shorts shorts = this.mongoTemplate.findById(ObjectIdUtil.convertToObjectId(shortsId), Shorts.class);
        if(shorts == null) {
            log.error("쇼츠 조회 실패: {}", shortsId);
            throw new DuckwhoException(ErrorCode.NOT_FOUND_SHORTS);
        }

        CommentDetail commentDetail = CommentDetail.builder()
                .commentText(shortsCommentCreateReqDTO.getComment())
                .replies(Collections.emptyList())
                .build();

        Interaction<CommentDetail> commentInteraction = Interaction.<CommentDetail>builder()
                .userId(user.getUserId())
                .shortsId(ObjectIdUtil.convertToObjectId(shortsId))
                .interactionType(InteractionType.COMMENT)
                .details(commentDetail)
                .shortsTags(shorts.getTags())
                .build();

        log.debug("댓글 생성 요청: {}", commentInteraction);

        try {
            mongoTemplate.save(commentInteraction);
            log.info("createShortsComment : {}", commentInteraction);
        } catch (Exception e) {
            log.error("Failed to create comment interaction: {}", e.getMessage(), e);
            if (commentInteraction != null) {
                mongoTemplate.remove(commentInteraction);
            }
            throw new DuckwhoException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ShortsResponseDTO findShortsInfo(String shortsId, Long userId) {
        Shorts shorts = shortsRepository.findById(shortsId)
                .orElseThrow(FileException.FileNotFoundException::new);

        if(shorts.getFileInfo() != null) {
            Shorts.VideoMetadata fileInfo = shorts.getFileInfo();
            String m3u8Url = fileInfo.getRemoteStorageUrl()
                    .stream()
                    .filter(fileUrl -> fileUrl.endsWith(".m3u8"))
                    .findFirst()
                    .orElseThrow(FileException.FileNotFoundException::new);
            ShortsLikeInteractionResponseDTO userInterAction = interactionRepository.isUserLikeInterAction(userId, shortsId);


            Shorts.PopularityMetric popularityMetric = shorts.getPopularityMetrics();
            // TODO 사용자 상호 작용 테이블에서 유저가 like, dislike 했는지 확인
            return ShortsResponseDTO.builder()
                    .shortsId(shortsId)
                    .profileImgUrl(shorts.getProfileImg())
                    .description(shorts.getDescription())
                    .userLikeInteraction(likeInteractionResponse)
                    .popularityMatic(new PopularityMaticResDTO(shorts.getPopularityMetrics()))
                    .popularityMatic(new PopularityMaticResDTO(popularityMetric))
                    .m3u8Url(m3u8Url)
                    .build();
        } else {
            throw new FileException.FileNotFoundException();
        }

    }

    /**
     * 댓글 수정
     * @param principalUser
     * @param shortsCommentUpdateReqDTO
     */
    @SuppressWarnings("unchecked")
    @Transactional
    @RequireUser
    @Override
    public void updateShortsComment(PrincipalUser principalUser, ShortsCommentUpdateReqDTO shortsCommentUpdateReqDTO, String commentId) {
        User user = principalUser.getUser();

        Interaction<CommentDetail> commentInteraction = Optional.ofNullable(this.mongoTemplate.findById(
                ObjectIdUtil.convertToObjectId(commentId),
                Interaction.class
        )).orElseThrow(() -> new DuckwhoException(ErrorCode.NOT_FOUND_SHORTS_COMMENT));

        // 인증되지 않은 접근
        if(commentInteraction.getUserId() != user.getUserId()) {
            throw new DuckwhoException(ErrorCode.UNAUTHORIZED_ACCESS);
        }

        // 댓글 수정
        Query query = new Query(
                Criteria
                        .where("_id").is(ObjectIdUtil.convertToObjectId(commentId))
                        .and("user_id").is(user.getUserId())
        );

        Update update = new Update();
        update.set("details.commentText", shortsCommentUpdateReqDTO.getComment());

        UpdateResult updateFirst = this.mongoTemplate.updateFirst(query, update, Interaction.class);
        log.info("댓글 수정 결과: {}", updateFirst);

        if(updateFirst.getModifiedCount() == 0) {
            throw new DuckwhoException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

    }

    /**
     * 댓글 삭제
     * @param principalUser
     * @param commentId
     */
    @SuppressWarnings("unchecked")
    @RequireUser
    @Override
    public void deleteShortsComment(PrincipalUser principalUser, String commentId) {
        User user = principalUser.getUser();

        Interaction<CommentDetail> commentInteraction = Optional.ofNullable(this.mongoTemplate.findById(
                ObjectIdUtil.convertToObjectId(commentId),
                Interaction.class
        )).orElseThrow(() -> new DuckwhoException(ErrorCode.NOT_FOUND_SHORTS_COMMENT));

        if(commentInteraction.getUserId() != user.getUserId()) {
            throw new DuckwhoException(ErrorCode.UNAUTHORIZED_ACCESS);
        }

        DeleteResult remove = this.mongoTemplate.remove(commentInteraction);
        log.debug("댓글 삭제 결과: {}", remove);

        if(remove.getDeletedCount() == 0) {
            throw new DuckwhoException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 대댓글 생성
     * @param principalUser
     * @param shortsCommentCreateReqDTO
     * @param commentId
     */

    @RequireUser
    @Override
    public void createShortsReply(PrincipalUser principalUser, ShortsCommentCreateReqDTO shortsCommentCreateReqDTO,
                                  String commentId) {

        User user = principalUser.getUser();

        Interaction<CommentDetail> commentInteraction = Optional.ofNullable(this.mongoTemplate.findById(
                ObjectIdUtil.convertToObjectId(commentId),
                Interaction.class
        )).orElseThrow(() -> new DuckwhoException(ErrorCode.NOT_FOUND_SHORTS_COMMENT));

        CommentDetail commentDetail = commentInteraction.getDetails();

        CommentDetail.Reply reply = CommentDetail.Reply.builder()
                .id(new ObjectId())
                .userId(user.getUserId())
                .replyText(shortsCommentCreateReqDTO.getComment())
                .createdAt(LocalDateTime.now())
                .build();

        commentDetail.getReplies().add(reply);

        try {
            this.mongoTemplate.save(commentInteraction);
        } catch (Exception e) {
            log.error("대댓글 생성 실패: {}", e.getMessage(), e);
            throw new DuckwhoException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }


    /**
     * 대댓글 삭제
     * @param principalUser
     * @param commentId
     * @param replyId
     */
    @Override
    @RequireUser
    @SuppressWarnings("unchecked")
    public void deleteShortsReply(PrincipalUser principalUser, String commentId, String replyId) {
        Long userId = principalUser.getUser().getUserId();

        Interaction<CommentDetail> commentInteraction = Optional.ofNullable(this.mongoTemplate.findById(
                ObjectIdUtil.convertToObjectId(commentId),
                Interaction.class
        )).orElseThrow(() -> new DuckwhoException(ErrorCode.NOT_FOUND_SHORTS_COMMENT));

        // 대댓글 찾기
        CommentDetail.Reply targetReply = commentInteraction.getDetails().getReplies().stream()
                .filter(reply -> reply.getId().toString().equals(replyId))
                .findFirst()
                .orElseThrow(() -> new DuckwhoException(ErrorCode.NOT_FOUND_SHORTS_REPLY));

        // 대댓글 작성자 확인
        if (!targetReply.getUserId().equals(userId)) {
            throw new DuckwhoException(ErrorCode.UNAUTHORIZED_ACCESS);
        }

        // 대댓글 삭제
        commentInteraction.getDetails().getReplies().remove(targetReply);
        this.mongoTemplate.save(commentInteraction);
    }

    /**
     * 대댓글 수정
     * @param principalUser
     * @param shortsCommentUpdateReqDTO
     * @param replyId
     */
    @Override
    @RequireUser
    @SuppressWarnings("unchecked")
    public void updateShortsReply(PrincipalUser principalUser, ShortsCommentUpdateReqDTO shortsCommentUpdateReqDTO,
                                  String commentId, String replyId) {

        Long userId = principalUser.getUser().getUserId();

        Interaction<CommentDetail> commentInteraction = Optional.ofNullable(this.mongoTemplate.findById(
                ObjectIdUtil.convertToObjectId(commentId),
                Interaction.class
        )).orElseThrow(() -> new DuckwhoException(ErrorCode.NOT_FOUND_SHORTS_COMMENT));

        // 대댓글 찾기
        CommentDetail.Reply targetReply = commentInteraction.getDetails().getReplies().stream()
                .filter(reply -> reply.getId().toString().equals(replyId))
                .findFirst()
                .orElseThrow(() -> new DuckwhoException(ErrorCode.NOT_FOUND_SHORTS_REPLY));

        // 대댓글 작성자 확인
        if (!targetReply.getUserId().equals(userId)) {
            throw new DuckwhoException(ErrorCode.UNAUTHORIZED_ACCESS);
        }

        // 대댓글 업데이트
        Query query = new Query(
                Criteria.where("_id").is(ObjectIdUtil.convertToObjectId(commentId))
                        .and("details.replies._id").is(new ObjectId(replyId))
        );

        Update update = new Update().set("details.replies.$.replyText", shortsCommentUpdateReqDTO.getComment());

        UpdateResult result = mongoTemplate.updateFirst(query, update, Interaction.class);
        log.info("대댓글 수정 결과: {}", result);
        if (result.getMatchedCount() == 0) {    // 매칭된 문서가 없는경우 에러처리
            throw new DuckwhoException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}
