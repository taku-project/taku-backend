package com.ani.taku_backend.post.service;

import com.ani.taku_backend.category.domain.entity.Category;
import com.ani.taku_backend.category.domain.repository.CategoryRepository;
import com.ani.taku_backend.common.annotation.RequireUser;
import com.ani.taku_backend.common.exception.DuckwhoException;
import com.ani.taku_backend.common.exception.PostException;
import com.ani.taku_backend.common.model.entity.Image;
import com.ani.taku_backend.common.service.FileService;
import com.ani.taku_backend.common.service.ImageService;
import com.ani.taku_backend.post.model.dto.PostCreateRequestDTO;
import com.ani.taku_backend.post.model.dto.PostListResponseDTO;
import com.ani.taku_backend.post.model.dto.PostUpdateRequestDTO;
import com.ani.taku_backend.post.model.entity.CommunityImage;
import com.ani.taku_backend.post.model.entity.Post;
import com.ani.taku_backend.post.repository.PostRepository;
import com.ani.taku_backend.user.model.dto.PrincipalUser;
import com.ani.taku_backend.user.model.entity.User;
import com.ani.taku_backend.user.service.BlackUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static com.ani.taku_backend.common.exception.ErrorCode.NOT_FOUND_CATEGORY;
import static com.ani.taku_backend.common.exception.ErrorCode.UNAUTHORIZED_ACCESS;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final CategoryRepository categoryRepository;
    private final ImageService imageService;
    private final FileService fileService;
    private final BlackUserService blackUserService;

    /**
     * 게시글 전체 조회 -> 삭제된 내역은 검색안되게 수정, 그리고 반환값에 전체 개수 반환
     */
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

    /**
     * 게시글 작성
     */
    @RequireUser
    @Transactional
    public Long createPost(PostCreateRequestDTO postCreateRequestDTO, List<MultipartFile> imageList, PrincipalUser principalUser) {

        User user = blackUserService.checkBlackUser(principalUser);             // 유저 검증
        Category category = checkCategory(postCreateRequestDTO, null);     // 카테고리 확인


        List<Image> saveImageList = imageService.saveImageList(imageList, user);     // 이미지 저장
        Post post = getPost(postCreateRequestDTO, user, category);                   // 게시글 생성
        setRelationCommunityImages(saveImageList, post);                             // 연관관계 설정

        Long savePostId = postRepository.save(post).getId();
        log.info("게시글 저장 완료, savePostId: {}", savePostId);

        return savePostId;
    }

    /**
     * 게시글 업데이트, 리펙토링 중
     */
    @RequireUser
    @Transactional
    public Long updatePost(PostUpdateRequestDTO postUpdateRequestDTO,Long postId, List<MultipartFile> imageList, PrincipalUser principalUser) {
        // 유저 정보 가져오기
        User user = principalUser.getUser();

        // 게시글 조회, 없으면 예외
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostException.PostNotFoundException("ID: " + postId));

        // 수정 권한 확인
        if (!user.getUserId().equals(post.getUser().getUserId())) {
            throw new PostException.PostAccessDeniedException("게시글을 수정할 권한이 없습니다.");
        }

        // 카테고리 확인
        Category newCategory = null;
        if (postUpdateRequestDTO.getCategoryId() != null && !postUpdateRequestDTO.getCategoryId().equals(post.getCategory().getId())) {
            newCategory = categoryRepository.findById(postUpdateRequestDTO.getCategoryId())
                    .orElseThrow(() -> new IllegalArgumentException("카테고리를 찾을 수 없습니다. ID: " + postUpdateRequestDTO.getCategoryId()));
        }

        // 이미지 수정
        updateImages(postUpdateRequestDTO, imageList, user, post);

        // 게시글 수정
        post.updatePost(postUpdateRequestDTO.getTitle(), postUpdateRequestDTO.getContent(), newCategory);

        return post.getId();
    }

    /**
     * 게시글 삭제
     */
    @RequireUser
    @Transactional
    public Long deletePost(Long postId, PrincipalUser principalUser) {
        User user = principalUser.getUser();
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostException.PostNotFoundException("ID: " + postId));

        if (!user.getUserId().equals(post.getUser().getUserId())) {
            throw new PostException.PostAccessDeniedException("게시글을 삭제할 권한이 없습니다.");
        }
        post.delete();
        post.getCommunityImages().forEach(communityImage -> {
            communityImage.getImage().delete();
        });

        return post.getId();
    }

    // 게시글 생성
    private Post getPost(PostCreateRequestDTO postCreateRequestDTO, User user, Category category) {
        return Post.builder()
                .user(user)
                .category(category)
                .title(postCreateRequestDTO.getTitle())
                .content(postCreateRequestDTO.getContent())
                .views(0L)
                .build();
    }

    // 커뮤니티 이미지 연관관계 설정
    private void setRelationCommunityImages(List<Image> saveImageList, Post post) {
        for (Image image : saveImageList) {
            CommunityImage communityImage = CommunityImage.builder()
                    .post(post)
                    .image(image)
                    .build();
            post.addCommunityImage(communityImage);
        }
    }

    // 카테고리 검증
    private Category checkCategory(PostCreateRequestDTO postCreateRequestDTO, Post post) {

        Category category = categoryRepository.findById(postCreateRequestDTO.getCategoryId())
                .orElseThrow(() -> new DuckwhoException(NOT_FOUND_CATEGORY));

        if (post != null && !post.getCategory().getId().equals(category.getId())) {
            throw new DuckwhoException(UNAUTHORIZED_ACCESS);
        }

        log.info("카테고리 검증 완료, 카테고리 이름 {}", category.getName());
        return category;
    }
}
