//package com.ani.taku_backend.post.repository;
//
//import com.ani.taku_backend.common.enums.ProviderType;
//import com.ani.taku_backend.common.enums.UserRole;
//import com.ani.taku_backend.common.model.entity.Image;
//import com.ani.taku_backend.common.repository.ImageRepository;
//import com.ani.taku_backend.post.model.entity.CommunityImage;
//import com.ani.taku_backend.post.model.entity.Post;
//import com.ani.taku_backend.user.model.entity.User;
//import com.ani.taku_backend.user.repository.UserRepository;
//import jakarta.persistence.EntityManager;
//import jakarta.persistence.PersistenceContext;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.annotation.Commit;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.concurrent.ThreadLocalRandom;
//
//@SpringBootTest
//class PostRepositoryTest {
//
//    @Autowired
//    private PostRepository postRepository;
//
//    @PersistenceContext
//    private EntityManager entityManager;
//
////    @Test
//    @Transactional
//    @Commit
//    void initSortTest() {
//        int totalPostRecords = 100000;
//        int postBatchSize = 10000;
//
//        for (int i = 1; i <= totalPostRecords; i++) {
//            Long views = ThreadLocalRandom.current().nextLong(1, 10000);
//            Long likes = ThreadLocalRandom.current().nextLong(1, 10000);
//
//            LocalDateTime createdTime = LocalDateTime.now().minusSeconds(totalPostRecords - i);
//            LocalDateTime updatedTime = createdTime.plusSeconds(ThreadLocalRandom.current().nextInt(0, 60));
//
//            Image image = new Image(null, null, null, "이미지경로" + i, null, null, null, null, null);
//
//            // 데이터 생성
//            Post post1 = createPost(i, createdTime, updatedTime, "title", "content", views, likes);
//            Post post2 = createPost(i, createdTime, updatedTime, "제목", "내용", views, likes);
//            Post post3 = createPost(i, createdTime, updatedTime, "사랑", "평화", views, likes);
//            Post post4 = createPost(i, createdTime, updatedTime, "진실", "거짓", views, likes);
//            Post post5 = createPost(i, createdTime, updatedTime, "개발", "공부", views, likes);
//
//            entityManager.persist(post1);
//            entityManager.persist(post2);
//            entityManager.persist(post3);
//            entityManager.persist(post4);
//            entityManager.persist(post5);
//
//            // 배치 처리
//            if (i % postBatchSize == 0) {
//                entityManager.flush();
//                entityManager.clear();
//            }
//        }
//
//        // 마지막 플러시
//        entityManager.flush();
//        entityManager.clear();
//    }
//
//    private Post createPost(int i, LocalDateTime createdTime, LocalDateTime updatedTime,  String title, String content, Long views, Long likes) {
//        Post post = new Post(null, null, null, null, title + i, content + i,
//                createdTime, updatedTime, views, likes, null);
//        return post;
//    }
//
////    @Test
//    void noOffsetPagingTest() {
//        Long randomId = (long) (Math.random() * 100000) + 1;
//        Post findPost = postRepository.findById(randomId).get();
//
//        latestFilterPagingTest(findPost);
//        viewsFilterPagingTest(findPost);
//        likesFilterPagingTest(findPost);
//
//    }
//
//    private void likesFilterPagingTest(Post findPost) {
//        long startTime = System.nanoTime();
//        List<Post> result = postRepository.findAllPostWithNoOffset("likes", findPost.getLikes(), false, 20, "제목");
//        long resultTime = (System.nanoTime() - startTime) / 1000000;
//
//        System.out.println("lastValue = " + findPost.getLikes());
//        System.out.println("resultTime(likes) = " + resultTime + "ms");
//        for (Post post : result) {
//            System.out.println("post.getId() = " + post.getId() + " post.getLikes() = " + post.getLikes());
//        }
//    }
//
//    private void viewsFilterPagingTest(Post findPost) {
//        long startTime = System.nanoTime();
//        List<Post> result = postRepository.findAllPostWithNoOffset("views", findPost.getViews(), false, 20, "거짓");
//        long resultTime = (System.nanoTime() - startTime) / 1000000;
//
//        System.out.println("lastValue = " + findPost.getViews());
//        System.out.println("resultTime(views) = " + resultTime + "ms");
//        for (Post post : result) {
//            System.out.println("post.getId() = " + post.getId() + " post.getViews() = " + post.getViews());
//        }
//    }
//
//    private void latestFilterPagingTest(Post findPost) {
//        long startTime = System.nanoTime();
//        List<Post> result = postRepository.findAllPostWithNoOffset("latest", findPost.getId(), false, 20, "사랑");
//        long resultTime = (System.nanoTime() - startTime) / 1000000;
//
//        System.out.println("lastValue = " + findPost.getId());
//        System.out.println("resultTime(latest) = " + resultTime + "ms");
//        for (Post post : result) {
//            System.out.println("post.getId() = " + post.getId());
//        }
//    }
//
//    @Test
//    @Transactional
//    @Commit
//    void initImageUrlTest() {
//        // 유저 생성
//        User user = User.builder()
//                .nickname("testUser")
//                .providerType("KAKAO")
//                .profileImg("https://example.com/profile.jpg")
//                .status("ACTIVE")
//                .domesticId("user123")
//                .gender("M")
//                .ageRange("20-30")
//                .role("USER")
//                .email("test@example.com")
//                .build();
//        entityManager.persist(user);
//
//        // 이후 이미지 및 게시글 생성 로직
//        for (int i = 1; i <= 10000; i++) {
//            Post post = Post.builder()
//                    .title("Post Title " + i)
//                    .content("Post Content " + i)
//                    .views((long) (Math.random() * 1000))
//                    .likes((long) (Math.random() * 500))
//                    .createdAt(LocalDateTime.now())
//                    .updatedAt(LocalDateTime.now())
//                    .build();
//            entityManager.persist(post);
//
//            Image image = Image.builder()
//                    .user(user)  // 생성된 User와 연결
//                    .fileName("image" + i + ".jpg")
//                    .imageUrl("https://example.com/image" + i + ".jpg")
//                    .originalName("Original Image " + i)
//                    .fileType("image/jpeg")
//                    .fileSize(1024)
//                    .build();
//            entityManager.persist(image);
//
//            CommunityImage communityImage = CommunityImage.builder()
//                    .post(post)
//                    .image(image)
//                    .build();
//            entityManager.persist(communityImage);
//        }
//        entityManager.flush();
//        entityManager.clear();
//    }
//
//    @Test
//    void imageUrlTest() {
//        Long randomId = (long) (Math.random() * 10000) + 1;
//        Post findPost = postRepository.findById(randomId).get();
//
//        List<Post> result = postRepository.findAllPostWithNoOffset("latest", findPost.getId(), false, 20, null);
//        for (Post post : result) {
//            System.out.println("post.getId() = " + post.getId() + " ImageUrl = " + post.getCommunityImages().get(0).getImage().getImageUrl());
//        }
//    }
//
//}