package com.ani.taku_backend.post.service;


import com.ani.taku_backend.common.enums.InteractionType;
import com.ani.taku_backend.post.model.entity.Post;
import com.ani.taku_backend.post.repository.PostRepository;
import com.ani.taku_backend.user.model.entity.User;
import com.ani.taku_backend.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.LongStream;

import static com.ani.taku_backend.post.model.entity.QPost.post;

@SpringBootTest
class PostInteractionServiceTest {

    @Autowired PostInteractionService postInteractionService;
    @Autowired PostRepository postRepository;
    @Autowired UserRepository userRepository;

    private static final int THREAD_COUNT = 10;  // 동시에 실행할 스레드 개수
    private static final int LIKES_PER_USER = 10; // 한 유저당 좋아요를 누를 게시글 수

    @Test
    void postLikesTest() throws InterruptedException {
        List<User> allUser = userRepository.findAll();
        List<Post> allPost = postRepository.findAll();

        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);

        for (int i = 0; i < THREAD_COUNT; i++) {
            executorService.execute(() -> {
                try {
                    for (int j = 0; j < LIKES_PER_USER; j++) {
                        int userIndex = ThreadLocalRandom.current().nextInt(allUser.size());
                        int postIndex = ThreadLocalRandom.current().nextInt(allPost.size());

                        User randomUser = allUser.get(userIndex);
                        Post randomPost = allPost.get(postIndex);

                        long likes = postInteractionService.togglePostLike(randomPost.getId(), randomUser, InteractionType.LIKE);
                        System.out.println("UserId: " + randomUser.getUserId() +
                                " | PostId: " + randomPost.getId() +
                                " | Likes: " + likes);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown(); // 완료된 스레드 수 감소
                }
            });
        }

        latch.await(); // 모든 스레드가 종료될 때까지 대기
        executorService.shutdown();
    }


    @Test
    void initializeMongoDBLikeData() throws InterruptedException {
        // 1. 테스트용 유저 가져오기 (첫 번째 유저 사용)
        User testUser = userRepository.findAll().get(0); // 테스트용 유저 하나만 사용

        // 2. 전체 게시글 가져오기
        List<Post> allPosts = postRepository.findAll();

        // 3. 멀티스레드로 전체 게시글에 대해 좋아요 2번씩 눌러서 초기화
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
        CountDownLatch latch = new CountDownLatch(allPosts.size()); // 모든 작업이 끝날 때까지 대기

        for (Post post : allPosts) {
            executorService.execute(() -> {
                try {
                    // 같은 유저가 같은 게시글에 좋아요 2번 눌러서 0으로 초기화
                    postInteractionService.togglePostLike(post.getId(), testUser, InteractionType.LIKE);
                    postInteractionService.togglePostLike(post.getId(), testUser, InteractionType.LIKE);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown(); // 완료된 스레드 수 감소
                }
            });
        }

        latch.await(); // 모든 스레드가 종료될 때까지 대기
        executorService.shutdown();
    }
}