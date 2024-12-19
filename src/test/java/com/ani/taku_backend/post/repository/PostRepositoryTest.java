package com.ani.taku_backend.post.repository;

import com.ani.taku_backend.post.model.entity.Post;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@SpringBootTest
class PostRepositoryTest {

    @Autowired private PostRepository postRepository;

    @PersistenceContext
    private EntityManager entityManager;

//    @Test
    @Transactional
    @Commit
    void saveTestData() {
        int totalRecords = 1000000;
        int batchSize = 100000;

        for (int i = 1; i <= totalRecords; i++) {
            int views = ThreadLocalRandom.current().nextInt(1, 300000);
            int likes = ThreadLocalRandom.current().nextInt(1, 100000);

            LocalDateTime createdTime = LocalDateTime.now().minusSeconds(totalRecords - i);
            LocalDateTime updatedTime = createdTime.plusSeconds(ThreadLocalRandom.current().nextInt(0, 60));

            // 데이터 생성
            Post post = new Post(null, null, null, "title" + i, "content" + i,
                    createdTime, updatedTime, views, likes);

            entityManager.persist(post);

            // 배치 처리
            if (i % batchSize == 0) {
                entityManager.flush();
                entityManager.clear();
            }
        }

        // 마지막 플러시
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void noOffsetPagingTest() {
        Long randomId = (long) (Math.random() * 1000000) + 1;
        Post findPost = postRepository.findById(randomId).get();

        latestFilterPagingTest(findPost);
        viewsFilterPagingTest(findPost);
        likesFilterPagingTest(findPost);

    }

    private void likesFilterPagingTest(Post findPost) {
        long startTime = System.nanoTime();
        List<Post> result = postRepository.findAllPostWithNoOffset("likes", findPost.getLikes(), false, 20, null);
        long resultTime = (System.nanoTime() - startTime) / 1000000;

        System.out.println("resultTime(likes) = " + resultTime + "ms");
        for (Post post : result) {
            System.out.println("post.getId() = " + post.getId() + " post.getLikes() = " + post.getLikes());
        }
    }

    private void viewsFilterPagingTest(Post findPost) {
        long startTime = System.nanoTime();
        List<Post> result = postRepository.findAllPostWithNoOffset("views", findPost.getViews(), false, 20, null);
        long resultTime = (System.nanoTime() - startTime) / 1000000;

        System.out.println("resultTime(views) = " + resultTime + "ms");
        for (Post post : result) {
            System.out.println("post.getId() = " + post.getId() + " post.getViews() = " + post.getViews());
        }
    }

    private void latestFilterPagingTest(Post findPost) {
        long startTime = System.nanoTime();
        List<Post> result = postRepository.findAllPostWithNoOffset("latest", findPost.getCreatedAt(), false, 20, null);
        long resultTime = (System.nanoTime() - startTime) / 1000000;

        System.out.println("resultTime(latest) = " + resultTime + "ms");
        for (Post post : result) {
            System.out.println("post.getId() = " + post.getId() + " post.getCreatedAt() = " + post.getCreatedAt());
        }
    }

}