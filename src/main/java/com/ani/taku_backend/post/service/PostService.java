package com.ani.taku_backend.post.service;

import com.ani.taku_backend.category.domain.entity.Category;
import com.ani.taku_backend.category.domain.repository.CategoryRepository;
import com.ani.taku_backend.common.annotation.RequireUser;
import com.ani.taku_backend.common.model.dto.ImageCreateRequestDTO;
import com.ani.taku_backend.common.model.entity.Image;
import com.ani.taku_backend.common.repository.ImageRepository;
import com.ani.taku_backend.post.model.dto.PostCreateRequestDTO;
import com.ani.taku_backend.post.model.dto.PostListResponseDTO;
import com.ani.taku_backend.post.model.entity.CommunityImage;
import com.ani.taku_backend.post.model.entity.Post;
import com.ani.taku_backend.post.repository.PostRepository;
import com.ani.taku_backend.user.model.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final CategoryRepository categoryRepository;
    private final ImageRepository imageRepository;

    public List<PostListResponseDTO> findAllPost(String filter, Long lastValue, boolean isAsc, int limit, String keyword, Long categoryId) {

        /**
         * 검증 로직
         * - 공백만 있는 keyword null 처리
         * - 공백 제거(양옆, 중간)
         */
        if (keyword != null) {
            keyword = keyword.trim().isEmpty() ? null : keyword.replaceAll("\\s+", "");
        }
        List<Post> allPost = postRepository.findAllPostWithNoOffset(filter, lastValue, isAsc, limit, keyword, categoryId);
        return allPost.stream().map(PostListResponseDTO::new).toList();
    }

    @RequireUser
    public Long createPost(PostCreateRequestDTO requestDTO, User user) {

        Category category = categoryRepository.findById(requestDTO.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("카테고리를 찾을 수 없습니다. ID: " + requestDTO.getCategoryId()));

        Post post = savePost(requestDTO, user, category);

        if (requestDTO.getImagelist() != null && !requestDTO.getImagelist().isEmpty()) {
            saveImage(requestDTO, user, post);
        }
        postRepository.save(post);
        return post.getId();
    }

    private void saveImage(PostCreateRequestDTO requestDTO, User user, Post post) {
        for (ImageCreateRequestDTO imageRequest : requestDTO.getImagelist()) {
            Image image = Image.builder()
                    .user(user)
                    .fileName(imageRequest.getFileName())
                    .imageUrl(imageRequest.getImageUrl())
                    .originalName(imageRequest.getOriginalFileName())
                    .fileType(imageRequest.getFileType())
                    .fileName(imageRequest.getFileName())
                    .fileSize(imageRequest.getFileSize())
                    .deletedAt(null)
                    .build();
            imageRepository.save(image);

            post.addCommunityImage(
                    CommunityImage.builder()
                            .image(image)
                            .build());
        }
    }

    private Post savePost(PostCreateRequestDTO requestDTO, User user, Category category) {
        Post post = Post.builder()
                .user(user)
                .category(category)
                .title(requestDTO.getTitle())
                .content(requestDTO.getContent())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .views(0L)
                .likes(0L)
                .deletedAt(null)
                .build();
        return post;
    }
}
