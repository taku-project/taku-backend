package com.ani.taku_backend.post.service;

import com.ani.taku_backend.category.domain.entity.Category;
import com.ani.taku_backend.category.domain.repository.CategoryRepository;
import com.ani.taku_backend.common.annotation.RequireUser;
import com.ani.taku_backend.common.annotation.ValidateProfanity;
import com.ani.taku_backend.common.enums.UserRole;
import com.ani.taku_backend.common.exception.DuckwhoException;
import com.ani.taku_backend.common.model.entity.Image;
import com.ani.taku_backend.common.service.FileService;
import com.ani.taku_backend.common.service.ImageService;
import com.ani.taku_backend.post.model.dto.PostCreateRequestDTO;
import com.ani.taku_backend.post.model.dto.PostListRequestDTO;
import com.ani.taku_backend.post.model.dto.PostListResponseDTO;
import com.ani.taku_backend.post.model.dto.PostUpdateRequestDTO;
import com.ani.taku_backend.post.model.entity.CommunityImage;
import com.ani.taku_backend.post.model.entity.Post;
import com.ani.taku_backend.post.repository.PostRepository;
import com.ani.taku_backend.post.repository.impl.dto.FindAllPostQuerydslDTO;
import com.ani.taku_backend.user.model.dto.PrincipalUser;
import com.ani.taku_backend.user.model.entity.User;
import com.ani.taku_backend.user.service.BlackUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static com.ani.taku_backend.common.exception.ErrorCode.*;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final CategoryRepository categoryRepository;
    private final ImageService imageService;

    /**
     * 게시글 전체 조회
     */
    public PostListResponseDTO findAllPostList(PostListRequestDTO postListRequestDTO) {

        // 검색 키워드 검증 - 공백제거(양옆, 중간), 공백만 넘어온 키워드 null처리
        String keyword = postListRequestDTO.getKeyword();
        if (keyword != null) {
            keyword = keyword.trim().isEmpty() ? null : keyword.replaceAll("\\s+", "");
        }

        long postCount = postRepository.countPostByDeletedAtIsNullAndCategoryId(postListRequestDTO.getCategoryId());
        log.info("post 전체개수 조회, postCount: {}", postCount);

        List<FindAllPostQuerydslDTO> fineResultList = postRepository.findAllPostWithNoOffset(postListRequestDTO);
        log.info("페이징 쿼리 성공");

        return new PostListResponseDTO(postCount, fineResultList);
    }

    /**
     * 게시글 작성
     */
    @Transactional
    @ValidateProfanity(fields = {"title", "content"})
    public Long createPost(PostCreateRequestDTO postCreateRequestDTO, User user) {

        Category category = checkCategory(postCreateRequestDTO.getCategoryId(), null);     // 카테고리 확인

        List<MultipartFile> imageList = postCreateRequestDTO.getImageList();
        List<Image> saveImageList = imageService.saveImageList(imageList, user);     // 이미지 저장

        Post post = getPost(postCreateRequestDTO, user, category);                   // 게시글 생성
        setRelationCommunityImages(saveImageList, post);                             // 연관관계 설정

        Long savePostId = postRepository.save(post).getId();
        log.debug("게시글 저장 완료, savePostId: {}", savePostId);

        return savePostId;
    }

    /**
     * 게시글 업데이트
     */
    @Transactional
    @ValidateProfanity(fields = {"title", "content"})
    public Long updatePost(Long postId, PostUpdateRequestDTO postUpdateRequestDTO, User user) {

        // 게시글 조회, 없으면 예외
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new DuckwhoException(NOT_FOUND_POST));

        checkAuthorAndAdmin(user, post);             // 수정 권한 확인
        checkDeleteProduct(post);                    // 삭제 검증
        Category itemCategory = checkCategory(postUpdateRequestDTO.getCategoryId(), null);     // 카테고리 확인

        // 이미지 업데이트
        List<Image> newImageList = imageService.getUpdateImageList(postUpdateRequestDTO.getDeleteImageUrl(), postUpdateRequestDTO.getImageList(), user);

        if (!newImageList.isEmpty()) {
            setRelationCommunityImages(newImageList, post);         // 연관관계 설정 -> 이미지 저장
        }

        post.updatePost(postUpdateRequestDTO, itemCategory);         // 게시글 수정
        log.debug("게시글 수정 성공, postId: {}", postId);

        return post.getId();
    }

    /**
     * 게시글 삭제
     */
    @RequireUser
    @Transactional
    public void deletePost(Long postId, long categoryId, User user) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new DuckwhoException(NOT_FOUND_POST));

        checkAuthorAndAdmin(user, post);             // 수정 권한 확인
        checkDeleteProduct(post);                    // 삭제 검증
        checkCategory(categoryId, post);             // 카테고리 검증

        post.delete();                               // 삭제 로직
        log.debug("게시글 삭제 성공, post.getDeletedAt: {}", post.getDeletedAt());
        post.getCommunityImages().forEach(communityImage -> {
            communityImage.getImage().delete();
            log.debug("이미지 연관관계 삭제 성공, image.getDeletedAt: {}", communityImage.getImage().getDeletedAt());
        });


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
            log.debug("이미지 연관관계 적용 성공, communityImage.getImage().getId: {}", communityImage.getImage().getId());
        }
    }

    // 카테고리 검증
    private Category checkCategory(long categoryId, Post post) {

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new DuckwhoException(NOT_FOUND_CATEGORY));

        if (post != null && !post.getCategory().getId().equals(category.getId())) {
            throw new DuckwhoException(UNAUTHORIZED_ACCESS);
        }

        log.debug("카테고리 검증 완료, 카테고리 이름 {}", category.getName());
        return category;
    }

    // 어드민이거나, 작성자와 다르면 예외
    private void checkAuthorAndAdmin(User user, Post post) {
        if ((!user.getRole().equals(UserRole.ADMIN.name())) &&
                !user.getUserId().equals(post.getUser().getUserId())) {
            throw new DuckwhoException(UNAUTHORIZED_ACCESS);
        }
    }

    // 삭제된 글이면 예외
    private void checkDeleteProduct(Post post) {
        if (post.getDeletedAt() != null) {
            throw new DuckwhoException(NOT_FOUND_POST);
        }
    }

}
