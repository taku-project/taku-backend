//package com.ani.taku_backend.post.service;
//
//import com.ani.taku_backend.common.annotation.RequireUser;
//import com.ani.taku_backend.common.enums.InteractionType;
//import com.ani.taku_backend.common.exception.DuckwhoException;
//import com.ani.taku_backend.common.service.RedisService;
//import com.ani.taku_backend.post.model.entity.Post;
//import com.ani.taku_backend.post.model.entity.PostInteraction;
//import com.ani.taku_backend.post.model.entity.PostInteractionCounter;
//import com.ani.taku_backend.post.repository.PostRepository;
//import com.ani.taku_backend.post.repository.PostInteractionCounterRepository;
//import com.ani.taku_backend.post.repository.PostInteractionRepository;
//import com.ani.taku_backend.user.model.dto.PrincipalUser;
//import com.ani.taku_backend.user.model.entity.User;
//import com.ani.taku_backend.user.service.BlackUserService;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.Duration;
//import java.util.Optional;
//
//import static com.ani.taku_backend.common.exception.ErrorCode.*;
//
//@Slf4j
//@Service
//@RequiredArgsConstructor
//public class PostInteractionService {
//
//    private final PostInteractionRepository interactionRepository;
//    private final PostInteractionCounterRepository counterRepository;
//    private final PostRepository postRepository;
//
//    private final BlackUserService blackUserService;
//
//    private static final String REQUEST_COUNT_KEY = "post:%d:user:%d:count";        // 카운트?
//    private static final String REQUEST_LOCK_KEY = "post:%d:user:%d:lock";          // 락?
//    private static final Duration LOCK_TIME = Duration.ofSeconds(10); // 요청 차단 시간
//    private final RedisService redisService;
//
//    /**
//     * 좋아요 추가 / 취소
//     */
//    @Transactional
//    @RequireUser
//    public long togglePostLike(Long postId, PrincipalUser principalUser, InteractionType type) {
//
//        // 블랙 유저인지 검증
//        User user = blackUserService.checkBlackUser(principalUser);
//
//        // 게시글이 없으면 예외
//        Post findPost = postRepository.findById(postId).orElseThrow(() ->
//                new DuckwhoException(NOT_FOUND_POST)
//        );
//
//        if (findPost.getDeletedAt() != null) {  // 글 삭제된 게시
//            throw new DuckwhoException(NOT_FOUND_POST);
//        }
//
//        log.debug("게시글 좋아요 검증로직 통과");
//
//        String countKey = String.format(REQUEST_COUNT_KEY, findPost.getId(), user.getUserId());
//        String lockKey = String.format(REQUEST_LOCK_KEY, findPost.getId(), user.getUserId());
//
//        String currentCount = redisService.getKeyValue(countKey);                       // 레디스에 좋아요 누른 횟수가 있는지 확인
//        int requestCount = currentCount != null ? Integer.parseInt(currentCount) : 0;   // null이 아니면 개수를 int로 반환, 아니면 0으로 반환
//
//        // lockKey가 이미 레디스에 있으면 lock 상태
//        if (redisService.getKeyValue(lockKey) != null) {
//            throw new DuckwhoException(TOO_FAST_REQUEST);
//        }
//
//        // 상호작용 찾기
//        Optional<PostInteraction> findInteraction = interactionRepository.findByPostIdAndUserId(findPost.getId(), user.getUserId());
//
//        // 카운터 찾기
//        PostInteractionCounter postInteractionCounter = counterRepository.findById(findPost.getId())
//                .orElse(null);  // 먼저 카운터를 조회
//
//        // 카운터가 없으면 초기화
//        if (postInteractionCounter == null) {
//            postInteractionCounter = PostInteractionCounter.create(findPost.getId());
//            counterRepository.save(postInteractionCounter);
//            log.debug("카운터 초기화 성공");
//        }
//
//        postLikeUpdate(type, findInteraction, findPost, user, requestCount, lockKey, countKey);
//
//        return counterRepository.getPostLikes(findPost.getId());       // 좋아요 개수 반환
//    }
//
//    private void postLikeUpdate(InteractionType type, Optional<PostInteraction> findInteraction, Post findPost, User user, int requestCount, String lockKey, String countKey) {
//        if (findInteraction.isPresent()) {  // 상호작용이 이미 있으면 좋아요 취소, 즉 이미 좋아요를 눌렀던 상태
//            interactionRepository.delete(findInteraction.get());
//            counterRepository.decrementPostInteractionCounter(findPost.getId(), type);
//            log.debug("상호작용 삭제, 좋아요 제거");
//
//        } else {    // 그게 아니면 상호장호 등록하고 좋아요 증가
//            PostInteraction postInteraction = PostInteraction.of(findPost, user, type);
//            interactionRepository.save(postInteraction);
//            counterRepository.incrementPostInteractionCounter(findPost.getId(), type);
//            log.debug("상호작용 저장, 좋아요 증가");
//        }
//
//        // 2번까지 연속 클릭 허용 -> 이유는? 실수로 좋아요 한번 누른것은 취소할 수도 있어서(네이버 뉴스 댓글 좋아요 로직 참고함)
//        if (requestCount >= 2) {
//            redisService.setKeyValue(lockKey, "lock", LOCK_TIME);
//            redisService.deleteKeyValue(countKey);  // 카운트 초기화, 한번 락 걸리면 레디스의 countKey 내역은 초기화, lockKey는 10초가 지나면 자동으로 삭제됨
//            log.debug("2번 연속 입력, 락 상태");
//        } else {
//            redisService.setKeyValue(countKey, String.valueOf(requestCount + 1), LOCK_TIME);
//            log.debug("1번 연속 입력");
//        }
//    }
//}
