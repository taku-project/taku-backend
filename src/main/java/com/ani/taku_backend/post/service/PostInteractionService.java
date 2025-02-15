package com.ani.taku_backend.post.service;

import com.ani.taku_backend.common.enums.InteractionType;
import com.ani.taku_backend.common.exception.DuckwhoException;
import com.ani.taku_backend.common.service.RedisService;
import com.ani.taku_backend.post.model.entity.Post;
import com.ani.taku_backend.post.model.entity.PostInteraction;
import com.ani.taku_backend.post.model.entity.PostInteractionCounter;
import com.ani.taku_backend.post.repository.PostRepository;
import com.ani.taku_backend.post.repository.PostInteractionCounterRepository;
import com.ani.taku_backend.post.repository.PostInteractionRepository;
import com.ani.taku_backend.user.model.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Optional;

import static com.ani.taku_backend.common.exception.ErrorCode.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostInteractionService {

    private final PostInteractionRepository interactionRepository;
    private final PostInteractionCounterRepository counterRepository;
    private final PostRepository postRepository;
    private final RedisService redisService;

    private static final String REQUEST_COUNT_KEY = "post:%d:user:%d:count";        // 카운트
    private static final String REQUEST_LOCK_KEY = "post:%d:user:%d:lock";          // 락
    private static final Duration LOCK_TIME = Duration.ofSeconds(10); // 요청 차단 시간

    /**
     * 좋아요 추가 / 취소
     */
    @Transactional
    public long togglePostLike(Long postId, User user, InteractionType type) {
        log.debug("좋아요 토글 시작 - postId: {}, userId: {}, type: {}", postId, user.getUserId(), type);

        Post findPost = findPostWithValid(postId);          // 게시글이 없으면 예외
        handleRateLimit(user, findPost);                    // 3번이상 연속 클릭 시 10초 락

        // 상호작용 찾기
        Optional<PostInteraction> findInteraction = interactionRepository.findByPostIdAndUserId(findPost.getId(), user.getUserId());
        log.debug("기존 좋아요 상태 - exists: {}", findInteraction.isPresent());

        validCounter(findPost); // 카운터가 없으면 생성

        if (findInteraction.isPresent()) {
            log.debug("좋아요 취소 실행");
            cancelLike(findInteraction.get(), findPost.getId(), type);
        } else {
            log.debug("좋아요 추가 실행");
            addLike(findPost, user, type);
        }

        long likes = counterRepository.getPostLikes(findPost.getId());
        log.debug("최종 좋아요 수: {}", likes);
        return likes;
    }

    /**
     * 연속 입력 제한 핸들러
     */
    private void handleRateLimit(User user, Post findPost) {
        String countKey = String.format(REQUEST_COUNT_KEY, findPost.getId(), user.getUserId());
        String lockKey = String.format(REQUEST_LOCK_KEY, findPost.getId(), user.getUserId());

        String currentCount = redisService.getKeyValue(countKey);                       // 레디스에 좋아요 누른 횟수가 있는지 확인
        int requestCount = currentCount != null ? Integer.parseInt(currentCount) : 0;   // null이 아니면 개수를 int로 반환, 아니면 0으로 반환

        // lockKey가 이미 레디스에 있으면 lock 상태
        if (redisService.getKeyValue(lockKey) != null) {
            throw new DuckwhoException(TOO_FAST_REQUEST);
        }

        // 2번까지 연속 클릭 허용: 실수로 좋아요 한번 누른것은 취소할 수도 있어서(네이버 뉴스 댓글 좋아요 로직 참고)
        if (requestCount >= 2) {
            redisService.setKeyValue(lockKey, "lock", LOCK_TIME);
            redisService.deleteKeyValue(countKey);  // 카운트 초기화, 한번 락 걸리면 레디스의 countKey 내역은 초기화, lockKey는 10초가 지나면 자동으로 삭제
            log.debug("2번 연속 입력, 락 상태");
        } else {
            redisService.setKeyValue(countKey, String.valueOf(requestCount + 1), LOCK_TIME);
            log.debug("1번 연속 입력");
        }
    }

    /**
     * 게시글 조회 및 검증
     */
    private Post findPostWithValid(Long postId) {

        Post findPost = postRepository.findById(postId).orElseThrow(() ->
                new DuckwhoException(NOT_FOUND_POST)
        );

        if (findPost.getDeletedAt() != null) {  // 삭제된 글이면 예외
            throw new DuckwhoException(NOT_FOUND_POST);
        }
        return findPost;
    }

    /**
     * 좋아요 카운터 조회, 없으면 생성
     */
    private void validCounter(Post post) {
        boolean exists = counterRepository.existsById(post.getId());
        log.debug("좋아요 카운터 존재 여부: {}", exists);
        if (!exists) {
            log.debug("새로운 좋아요 카운터 생성 - postId: {}", post.getId());
            PostInteractionCounter newCounter = PostInteractionCounter.create(post);
            counterRepository.save(newCounter);
        }
    }

    /**
     * 좋아요 추가
     */
    private void addLike(Post post, User user, InteractionType type) {
        log.debug("좋아요 추가 - postId: {}, userId: {}", post.getId(), user.getUserId());
        PostInteraction interaction = PostInteraction.of(post, user, type);
        interactionRepository.save(interaction);
        counterRepository.incrementPostInteractionCounter(post.getId(), type);
        log.debug("좋아요 추가 완료");
    }

    /**
     * 좋아요 취소
     */
    private void cancelLike(PostInteraction interaction, Long postId, InteractionType type) {
        log.debug("좋아요 취소 - postId: {}, userId: {}", postId, interaction.getUserId());
        interactionRepository.delete(interaction);
        counterRepository.decrementPostInteractionCounter(postId, type);
        log.debug("좋아요 취소 완료");
    }
}
