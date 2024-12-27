package com.ani.taku_backend.post.service;

import com.ani.taku_backend.category.domain.entity.Category;
import com.ani.taku_backend.category.domain.repository.CategoryRepository;
import com.ani.taku_backend.common.enums.SortFilterType;
import com.ani.taku_backend.common.exception.PostException;
import com.ani.taku_backend.common.model.entity.Image;
import com.ani.taku_backend.common.repository.ImageRepository;
import com.ani.taku_backend.common.service.FileService;
import com.ani.taku_backend.post.model.dto.PostCreateUpdateRequestDTO;
import com.ani.taku_backend.post.model.dto.PostDetailResponseDTO;
import com.ani.taku_backend.post.model.dto.PostListResponseDTO;
import com.ani.taku_backend.post.model.entity.CommunityImage;
import com.ani.taku_backend.post.model.entity.Post;
import com.ani.taku_backend.post.repository.PostRepository;
import com.ani.taku_backend.user.model.dto.PrincipalUser;
import com.ani.taku_backend.user.model.entity.User;
import com.ani.taku_backend.user.repository.UserRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLOutput;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PostServiceTest {

    @Autowired
    PostService postService;

    @Autowired
    PostReadService postReadService;

    @Autowired
    PostRepository postRepository;

    @Autowired
    TestFixture testFixture;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ImageRepository imageRepository;

    //    @Test
    void init() {
        List<User> users = testFixture.createUser();
        List<Category> categories = testFixture.createCategory(users);
        List<Post> posts = testFixture.createPost(users, categories, 15000);
    }

    @Test
    void findAllPostsFilterLatest() {
        Random random = new Random();
        long lastValue = random.nextLong(1, 5001);
        List<PostListResponseDTO> postListResponseDTOS = postService.findAllPost("latest", lastValue, false, 20, "Post", 2L);

        System.out.println("lastValue = " + lastValue);
        for (PostListResponseDTO postListResponseDTO : postListResponseDTOS) {
            System.out.println("id = " + postListResponseDTO.getId() +
                            " title = " + postListResponseDTO.getTitle() +
                            " content = " + postListResponseDTO.getContent() +
                            " ImageUrl = " + postListResponseDTO.getImageUrl() +
                            " category" + postListResponseDTO.getCategoryId());

        }
    }

    @Test
    void findAllPostsFilterViews() {
        Random random = new Random();
        long lastValue = random.nextLong(1, 3000);
        List<PostListResponseDTO> postListResponseDTOS = postService.findAllPost("views", lastValue, false, 20, "tor", 3L);

        System.out.println("lastValue = " + lastValue);
        for (PostListResponseDTO postListResponseDTO : postListResponseDTOS) {
            System.out.println("views = " + postListResponseDTO.getViews() +
                            " id = " + postListResponseDTO.getId() +
                            " title = " + postListResponseDTO.getTitle() +
                            " content = " + postListResponseDTO.getContent() +
                            " ImageUrl = " + postListResponseDTO.getImageUrl() +
                            " category" + postListResponseDTO.getCategoryId());

        }
    }

    @Test
    void findAllPostsFilterLikes() {
        Random random = new Random();
        long lastValue = random.nextLong(1, 3000);
        List<PostListResponseDTO> postListResponseDTOS = postService.findAllPost("likes", lastValue, true, 20, "ong", 1L);

        System.out.println("lastValue = " + lastValue);
        for (PostListResponseDTO postListResponseDTO : postListResponseDTOS) {
            System.out.println("likes = " + postListResponseDTO.getLikes() +
                            " id = " + postListResponseDTO.getId() +
                            " title = " + postListResponseDTO.getTitle() +
                            " content = " + postListResponseDTO.getContent() +
                            " ImageUrl = " + postListResponseDTO.getImageUrl() +
                            " category" + postListResponseDTO.getCategoryId());

        }
    }

    @BeforeEach
    void mockAuthentication() {
        // Mock User 생성
        String email = "user2@example.com";

        // UserRepository에서 User 엔티티를 조회
        User user = userRepository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("User not found"));

        // PrincipalUser 객체 생성
        PrincipalUser principalUser = new PrincipalUser(user);

        // 권한 생성
        var authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));

        // SecurityContext에 PrincipalUser 설정
        var authentication = new UsernamePasswordAuthenticationToken(principalUser, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    @Transactional
    void createPost() {
        PrincipalUser principalUser = (PrincipalUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        PostCreateUpdateRequestDTO savePostDTO = new PostCreateUpdateRequestDTO();
        savePostDTO.setTitle("저장 제목1");
        savePostDTO.setContent("저장 내용1");
        savePostDTO.setCategoryId(4L);
        savePostDTO.setImagelist(null);

        Long postId = postService.createPost(savePostDTO, principalUser, null);

        Post post = postRepository.findById(postId).get();
        System.out.println("저장한 게시글 Id = " + postId + " Id로 조회한 게시글 Id = " + post.getId());
        System.out.println("게시글 작성자 = " + post.getUser().getNickname() + " 로그인 유저 = " + principalUser.getUser().getNickname());
        assertThat(post.getUser().getNickname()).isEqualTo(principalUser.getUser().getNickname());
    }

    @Test
    @Transactional
    void updatePost() {
        PrincipalUser principalUser = (PrincipalUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Post findPost = postRepository.findById(1L).get();
        String findTitle = findPost.getTitle();
        String findContent = findPost.getContent();

        PostCreateUpdateRequestDTO updatePostDTO = new PostCreateUpdateRequestDTO();
        updatePostDTO.setTitle("수정한 제목");
        updatePostDTO.setContent("수정한 내용");
        updatePostDTO.setCategoryId(2L);
        updatePostDTO.setImagelist(null);

        Long updatePostId = postService.updatePost(updatePostDTO, principalUser, findPost.getId(), null);

        Post updatePost = postRepository.findById(updatePostId).get();
        System.out.println("최초 제목 = " + findTitle + " 수정한 제목 = " + updatePost.getTitle());
        System.out.println("최초 내용 = " + findContent + " 수정한 내용 = " + updatePost.getContent());

        updatePost.getCommunityImages().forEach(communityImage -> {
            System.out.println("파일 이름 = " + communityImage.getImage().getFileName() +
                             " deleteAt = " + communityImage.getImage().getDeletedAt());
        });

        assertThat("수정한 제목").isEqualTo(updatePost.getTitle());
        assertThat("수정한 내용").isEqualTo(updatePost.getContent());
    }

    @Test
    @Transactional
    void deletePost() {
        PrincipalUser principalUser = (PrincipalUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        PostCreateUpdateRequestDTO savePostDTO = new PostCreateUpdateRequestDTO();
        savePostDTO.setTitle("삭제할 제목1");
        savePostDTO.setContent("삭제할 내용1");
        savePostDTO.setCategoryId(1L);
        savePostDTO.setImagelist(null);
        Long savePostId = postService.createPost(savePostDTO, principalUser, null);
        Long deletePostId = postService.deletePost(savePostId, principalUser);

        Post post = postRepository.findById(deletePostId).get();
        System.out.println("삭제 일시 = " + post.getDeletedAt());

        assertThat(post.getDeletedAt()).isNotNull();
    }

    @Test
    void findPostDetail() {
        Random random = new Random();
        long randomPostId = random.nextLong(1, 3000);
        PostDetailResponseDTO postDTO = postReadService.getPostDetail(randomPostId, true, 1L);
        System.out.println("게시글Id = " + postDTO.getPostId() +
                " 제목 = " + postDTO.getTitle() +
                " 내용 = " + postDTO.getContent() +
                " 좋아요 수 = " + postDTO.getLikes() +
                " 조회수 = " + postDTO.getViewCount() +
                " 이미지 " + postDTO.getImageUrls());
    }
}