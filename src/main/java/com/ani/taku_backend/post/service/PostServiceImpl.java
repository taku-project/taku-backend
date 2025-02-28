package com.ani.taku_backend.post.service;

import com.ani.taku_backend.category.domain.entity.Category;
import com.ani.taku_backend.category.domain.repository.CategoryRepository;
import com.ani.taku_backend.comments.model.dto.CommentsResponseDTO;
import com.ani.taku_backend.comments.service.CommentsService;
import com.ani.taku_backend.common.aop.annotation.ValidateProfanity;
import com.ani.taku_backend.common.enums.SortFilterType;
import com.ani.taku_backend.common.enums.UserRole;
import com.ani.taku_backend.common.exception.DuckwhoException;
import com.ani.taku_backend.common.model.entity.Image;
import com.ani.taku_backend.common.service.ImageService;
import com.ani.taku_backend.common.service.RedisService;
import com.ani.taku_backend.post.model.dto.PopularPostItemDTO;
import com.ani.taku_backend.post.model.dto.PopularPostLiestRequestDTO;
import com.ani.taku_backend.post.model.dto.PostCreateRequestDTO;
import com.ani.taku_backend.post.model.dto.PostDetailResponseDTO;
import com.ani.taku_backend.post.model.dto.PostListRequestDTO;
import com.ani.taku_backend.post.model.dto.PostListResponseDTO;
import com.ani.taku_backend.post.model.dto.PostUpdateRequestDTO;
import com.ani.taku_backend.post.model.entity.CommunityImage;
import com.ani.taku_backend.post.model.entity.Post;

import com.ani.taku_backend.post.model.entity.PostInteractionCounter;
import com.ani.taku_backend.post.model.enums.PopularPeriodType;
import com.ani.taku_backend.post.repository.PostInteractionCounterRepository;
import com.ani.taku_backend.post.repository.PostRepository;
import com.ani.taku_backend.post.model.dto.FindPostQueryDTO;
import com.ani.taku_backend.user.model.entity.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.ani.taku_backend.common.exception.ErrorCode.*;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final CategoryRepository categoryRepository;
    private final PostInteractionCounterRepository counterRepository;
    private final ImageService imageService;
    private final CommentsService commentsService;
    private final PostInteractionCounterRepository postInteractionCounterRepository;
    private final RedisService redisService;
    private final ObjectMapper objectMapper;
    /**
     * 게시글 전체 조회
     */
    public PostListResponseDTO findPostList(PostListRequestDTO postListRequestDTO, Pageable pageable) {
        // 검색 키워드 검증 - 공백제거(양옆, 중간), 공백만 넘어온 키워드 null처리
        String keyword = postListRequestDTO.getKeyword();
        if (keyword != null) {
            keyword = keyword.trim().isEmpty() ? null : keyword.replaceAll("\\s+", "");
            postListRequestDTO.setKeyword(keyword);
        }

        Page<FindPostQueryDTO> getPostList = postRepository.findPostListPage(postListRequestDTO, pageable);
        updateLikesCount(getPostList.getContent());

        return new PostListResponseDTO(getPostList);
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

        PostInteractionCounter counter = PostInteractionCounter.create(post);
        counterRepository.save(counter);

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
    @Transactional
    public void deletePost(Long postId, User user) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new DuckwhoException(NOT_FOUND_POST));

        checkAuthorAndAdmin(user, post);             // 수정 권한 확인
        checkDeleteProduct(post);                    // 삭제 검증

        post.delete();                               // 삭제 로직
        post.getCommunityImages().forEach(communityImage -> {
            communityImage.getImage().delete();
        });

        counterRepository.updateDeletedAt(post);

    }

    /**
     * 게시글 상세 조회
     * - 게시글 정보와 함께 댓글 목록을 조회
     * - 조회수 증가 처리
     * - 삭제된 게시글 체크
     * - 좋아요 정보
     */
    @Transactional
    public PostDetailResponseDTO getPostDetail(Long postId, Long currentUserId) {
        Post findPost = postRepository.findByIdWithImages(postId)
                .orElseThrow(() -> new DuckwhoException(NOT_FOUND_POST));

        checkDeleteProduct(findPost);

        boolean isOwner = currentUserId != null && currentUserId.equals(findPost.getUser().getUserId());
        long likeCount = getPostLikeCount(postId);
        boolean isLiked = currentUserId != null && postInteractionCounterRepository.isPostLikedByUser(postId, currentUserId);

        List<CommentsResponseDTO> comments = commentsService.getPostComments(postId, currentUserId);
        long commentCount = commentsService.getCommentCount(postId);

        return new PostDetailResponseDTO(findPost, isOwner, comments, likeCount, isLiked, commentCount);
    }

    /**
     * 게시글 인기 글 조회
     * - 현재 20개 씩 가져오는 걸로 되어있음.
     */
    @Override
    public PopularPostLiestRequestDTO getPopularityPosts(PopularPeriodType periodType) {
        final String POPULAR_POST_KEY = "popular_key";
        try {
            List<Object> cachedPopularPosts = redisService.getValues(POPULAR_POST_KEY);

            if(cachedPopularPosts.isEmpty()) {
                List<PostInteractionCounter> popularPost = counterRepository.findPopularPost(periodType);
                List<Long> popularPostId = popularPost.stream().map(PostInteractionCounter::getPostId).toList();

                List<PopularPostItemDTO> popularityPosts = postRepository.findPopularityPosts(popularPostId);

                // 레디스에 값 저장
                redisService.setKeyValue(POPULAR_POST_KEY, popularityPosts, Duration.ofHours(1));

                return PopularPostLiestRequestDTO.builder()
                        .popularPosts(popularityPosts)
                        .period(periodType)
                        .build();
            } else {
                List<PopularPostItemDTO> popularityPosts = cachedPopularPosts.stream()
                        .map(redisPopularPostItem ->
                            objectMapper.convertValue(redisPopularPostItem, PopularPostItemDTO.class)
                        ).toList();

                return PopularPostLiestRequestDTO.builder()
                        .popularPosts(popularityPosts)
                        .period(periodType)
                        .build();
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new DuckwhoException(INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * MongoDB에서 게시글의 좋아요 수를 조회합니다.
     * @param postId 게시글 ID
     * @return 좋아요 수
     */
    private long getPostLikeCount(Long postId) {
        return postInteractionCounterRepository.getPostLikes(postId);
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


    // 정렬 필터 비교 메서드
    private boolean isSortType(String sortProperty, SortFilterType... validTypes) {
        return Arrays.stream(validTypes)
                .anyMatch(type -> type.getValue().equalsIgnoreCase(sortProperty));
    }


    /**
     * MongoDB에서 좋아요 개수를 가져와 DTO에 반영하는 메서드
     */
    private void updateLikesCount(List<FindPostQueryDTO> postList) {
        List<Long> postIds = postList.stream().map(FindPostQueryDTO::getId).toList();
        Map<Long, Long> likesMap = counterRepository.findLikesByPostIds(postIds);

        postList.forEach(dto -> dto.updateLikes(likesMap.getOrDefault(dto.getId(), 0L)));
    }
}
