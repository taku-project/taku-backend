//package com.ani.taku_backend.post.service;
//
//import com.ani.taku_backend.category.domain.entity.Category;
//import com.ani.taku_backend.category.domain.repository.CategoryRepository;
//import com.ani.taku_backend.common.model.entity.Image;
//import com.ani.taku_backend.common.repository.ImageRepository;
//import com.ani.taku_backend.common.service.FileService;
//import com.ani.taku_backend.post.model.entity.CommunityImage;
//import com.ani.taku_backend.post.model.entity.Post;
//import com.ani.taku_backend.post.repository.PostRepository;
//import com.ani.taku_backend.user.model.entity.User;
//import com.ani.taku_backend.user.repository.UserRepository;
//import org.springframework.stereotype.Component;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Random;
//
//@Component
//public class TestFixture {
//    private final UserRepository userRepository;
//    private final CategoryRepository categoryRepository;
//    private final PostRepository postRepository;
//    private final ImageRepository imageRepository;
//
//    public TestFixture(UserRepository userRepository, CategoryRepository categoryRepository, PostRepository postRepository, ImageRepository imageRepository) {
//        this.userRepository = userRepository;
//        this.categoryRepository = categoryRepository;
//        this.postRepository = postRepository;
//        this.imageRepository = imageRepository;
//    }
//
//    private final Random random = new Random();
//
//    List<User> createUser() {
//        // 유저 1 생성
//        User user1 = User.builder()
//                .nickname("User1")
//                .providerType("KAKAO")
//                .profileImg("https://example.com/user1.jpg")
//                .status("ACTIVE")
//                .domesticId("domestic123")
//                .gender("Male")
//                .ageRange("20-29")
//                .role("USER")
//                .email("user1@example.com")
//                .build();
//
//        // 유저 2 생성
//        User user2 = User.builder()
//                .nickname("User2")
//                .providerType("KAKAO")
//                .profileImg("user2.jpg")
//                .status("ACTIVE")
//                .domesticId("domestic456")
//                .gender("Female")
//                .ageRange("30-39")
//                .role("USER")
//                .email("user2@example.com")
//                .build();
//
//        // 저장
//        return userRepository.saveAll(List.of(user1, user2));
//    }
//
//    List<Category> createCategory(List<User> users) {
//
//        User user1 = users.get(0);
//        User user2 = users.get(1);
//
//        List<Category> categories = List.of(
//                Category.builder().name("나루토").user(user1).createdType("USER").status("ACTIVE").viewCount(random.nextLong(1, 20001)).build(),
//                Category.builder().name("원피스").user(user1).createdType("USER").status("ACTIVE").viewCount(random.nextLong(1, 20001)).build(),
//                Category.builder().name("귀멸의칼날").user(user2).createdType("USER").status("ACTIVE").viewCount(random.nextLong(1, 20001)).build(),
//                Category.builder().name("짱구").user(user2).createdType("USER").status("ACTIVE").viewCount(random.nextLong(1, 20001)).build()
//        );
//        return categoryRepository.saveAll(categories);
//    }
//
//    public List<Post> createPost(List<User> users, List<Category> categories, int count) {
//        String[] titles = {"Post", "Title", "Song"};
//        String[] contents = {"Content", "Story", "Lyrics"};
//
//        List<Post> posts = new ArrayList<>();
//
//        for (int i = 0; i < count; i++) {
//            User user = users.get(random.nextInt(users.size()));
//            Category category = categories.get(random.nextInt(categories.size()));
//
//            // 제목과 내용 템플릿 선택
//            int value = i % 3; // 3가지 템플릿 순환
//            String title = titles[value] + i;
//            String content = contents[value] + i;
//
//            Post post = Post.builder()
//                    .user(user)
//                    .category(category)
//                    .title(title)
//                    .content(content)
//                    .views(random.nextLong(1, 5001))
//                    .likes(random.nextLong(1, 5001))
//                    .build();
//
//            postRepository.save(post);
//
//
//            // 랜덤 이미지
//            int imageCount = random.nextInt(6);
//            for (int j = 0; j <= imageCount; j++) {
//                Image image = Image.builder()
//                        .user(user)
//                        .fileName("image_" + i + ".jpg")
//                        .imageUrl("https://example.com/image_" + i + ".jpg")
//                        .originalName("original_image_" + i + ".jpg")
//                        .fileType("jpg")
//                        .fileSize(random.nextInt(5000))
//                        .build();
//
//                imageRepository.save(image);
//
//                CommunityImage communityImage = CommunityImage.builder()
//                        .image(image)
//                        .post(post)
//                        .build();
//
//                post.addCommunityImage(communityImage);
//            }
//
//            posts.add(post);
//        }
//
//        return posts;
//    }
//}
