package com.ani.taku_backend.init;

import com.ani.taku_backend.comments.model.dto.CommentsCreateRequestDTO;
import com.ani.taku_backend.comments.repository.CommentsRepository;
import com.ani.taku_backend.comments.service.CommentsService;
import com.ani.taku_backend.post.model.entity.Post;
import com.ani.taku_backend.post.repository.PostRepository;
import com.ani.taku_backend.user.model.entity.User;
import com.ani.taku_backend.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@SpringBootTest
public class CommentsInit {

    private static final Logger logger = LoggerFactory.getLogger(CommentsInit.class);

    @Autowired
    CommentsService commentsService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PostRepository postRepository;

    @Autowired
    CommentsRepository commentsRepository;

    @Autowired
    EntityManager entityManager;

    @Test
    void init() throws InterruptedException {

        int batchSize = 500;
        int totalComments = 20000;
        int threadCount = 4;

        List<User> allUser = userRepository.findAll();
        List<Post> allPosts = postRepository.findAll();

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        for (int start = 0; start < totalComments; start += batchSize * threadCount) {
            for (int thread = 0; thread < threadCount; thread++) {

                executorService.submit(() -> {
                    List<CommentsCreateRequestDTO> commentsCreateRequestDTOS = new ArrayList<>();

                    for (int i = 0; i < batchSize; i++) {

                        Post post = allPosts.get(ThreadLocalRandom.current().nextInt(0, allPosts.size()));
                        List<Long> allCommentIds = entityManager.createQuery("select c.id from Comments c", Long.class).getResultList();

                        logger.info("allCommentIds : {}", allCommentIds.size());

                        CommentsCreateRequestDTO requestDTO = new CommentsCreateRequestDTO();
                        requestDTO.setPostId(post.getId());
                        if (!allCommentIds.isEmpty()) {
                            Long randomCommentsId = allCommentIds.get(ThreadLocalRandom.current().nextInt(0, allCommentIds.size()));
                            requestDTO.setContent(randomCommentsId + " 에 대댓글 달아요");
                            requestDTO.setParentCommentId(randomCommentsId);
                        } else {
                            requestDTO.setContent(post.getTitle() + " 에 댓글 달아요");
                        }

                        commentsCreateRequestDTOS.add(requestDTO);
                    }

                    for (CommentsCreateRequestDTO requestDTO : commentsCreateRequestDTOS) {

                        User user = allUser.get(ThreadLocalRandom.current().nextInt(0, allUser.size()));
                        commentsService.createComments(requestDTO, user);

                    }
                });
            }
        }

        logger.info("전체 배치 작업 시작");
        executorService.shutdown();
        if (!executorService.awaitTermination(1, TimeUnit.HOURS)) {
            logger.error("작업이 시간 내에 종료되지 않았습니다.");
        } else {
            logger.info("전체 배치 작업 종료");
        }

    }
}
