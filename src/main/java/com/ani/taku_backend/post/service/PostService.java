package com.ani.taku_backend.post.service;

import com.ani.taku_backend.category.domain.entity.Category;
import com.ani.taku_backend.category.domain.repository.CategoryRepository;
import com.ani.taku_backend.common.annotation.RequireUser;
import com.ani.taku_backend.common.exception.FileException;
import com.ani.taku_backend.common.exception.PostException;
import com.ani.taku_backend.common.model.dto.ImageCreateRequestDTO;
import com.ani.taku_backend.common.model.entity.Image;
import com.ani.taku_backend.common.repository.ImageRepository;
import com.ani.taku_backend.post.model.dto.PostCreateRequestDTO;
import com.ani.taku_backend.post.model.dto.PostListResponseDTO;
import com.ani.taku_backend.post.model.dto.PostUpdateRequestDTO;
import com.ani.taku_backend.post.model.entity.CommunityImage;
import com.ani.taku_backend.post.model.entity.Post;
import com.ani.taku_backend.post.repository.PostRepository;
import com.ani.taku_backend.user.model.dto.PrincipalUser;
import com.ani.taku_backend.user.model.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final CategoryRepository categoryRepository;
    private final ImageRepository imageRepository;

    /**
     * 게시글 전체 조회
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
    public Long createPost(PostCreateRequestDTO postCreateRequestDTO, PrincipalUser principalUser) {
        User user = principalUser.getUser();

        Category category = categoryRepository.findById(postCreateRequestDTO.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("카테고리를 찾을 수 없습니다. ID: " + postCreateRequestDTO.getCategoryId()));

        Post post = getPost(postCreateRequestDTO, user, category);

        if (postCreateRequestDTO.getImagelist() != null && !postCreateRequestDTO.getImagelist().isEmpty()) {
            validateImageCount(postCreateRequestDTO.getImagelist());    // 5개 이상이면 예외 발생
            saveImage(postCreateRequestDTO, user, post);
        }
        postRepository.save(post);
        return post.getId();
    }

    /**
     * 게시글 업데이트
     */
    @RequireUser
    @Transactional
    public Long updatePost(PostUpdateRequestDTO postUpdateRequestDTO, PrincipalUser principalUser, Long postId) {
        User user = principalUser.getUser();
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostException.PostNotFoundException("ID: " + postId));

        if (!user.getUserId().equals(post.getUser().getUserId())) {
            throw new PostException.PostAccessDeniedException("게시글을 수정할 권한이 없습니다.");
        }
        Category newCategory = null;
        if (postUpdateRequestDTO.getCategoryId() != null && !postUpdateRequestDTO.getCategoryId().equals(post.getCategory().getId())) {
            newCategory = categoryRepository.findById(postUpdateRequestDTO.getCategoryId())
                    .orElseThrow(() -> new IllegalArgumentException("카테고리를 찾을 수 없습니다. ID: " + postUpdateRequestDTO.getCategoryId()));
        }
        post.updatePost(postUpdateRequestDTO.getTitle(), postUpdateRequestDTO.getContent(), newCategory);
        validateImageCount(postUpdateRequestDTO.getImagelist());    // 5개 이상이면 예외 발생
        updateImage(postUpdateRequestDTO, user, post);

        return post.getId();
    }

    /**
     * 게시글 삭제
     */
    @Transactional
    public Long deletePost(Long postId, PrincipalUser principalUser) {
        User user = principalUser.getUser();
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostException.PostNotFoundException("ID: " + postId));

        if (!user.getUserId().equals(post.getUser().getUserId())) {
            throw new PostException.PostAccessDeniedException("게시글을 삭제할 권한이 없습니다.");
        }
        post.softDelete();
        post.getCommunityImages().forEach(communityImage -> {
            communityImage.getImage().softDelete();
            post.removeCommunityImage(communityImage);
        });

        return post.getId();
    }

    private void saveImage(PostCreateRequestDTO postCreateRequestDTO, User user, Post post) {
        for (ImageCreateRequestDTO getImage : postCreateRequestDTO.getImagelist()) {
            Image image = Image.builder()
                    .user(user)
                    .fileName(getImage.getFileName())
                    .imageUrl(getImage.getImageUrl())
                    .originalName(getImage.getOriginalFileName())
                    .fileType(getImage.getFileType())
                    .fileName(getImage.getFileName())
                    .fileSize(getImage.getFileSize())
                    .deletedAt(null)
                    .build();
            imageRepository.save(image);

            post.addCommunityImage(CommunityImage
                    .builder()
                    .image(image)
                    .build());
        }
    }

    private void updateImage(PostUpdateRequestDTO postUpdateRequestDTO, User user, Post post) {
        List<String> fileNameByPostIdList = imageRepository.findFileNamesByPostId(post.getId());

        // 기존 게시글에 저장된 이미지와 요청으로 들어온 이미지의 파일네임을 비교하여 같은것과 다른 것을 분리
        Map<Boolean, List<ImageCreateRequestDTO>> partitionedImages = postUpdateRequestDTO.getImagelist()
                .stream()
                .collect(Collectors.partitioningBy(
                        imageDTO -> fileNameByPostIdList.contains(imageDTO.getFileName())));

        // 이미지 삭제 대상: 기존 게시글에 저장된 이미지와 분리된 파티션의 fasle인 대상중에서 요청으로 넘어온 대상을 제외한 대상
        List<String> deleteFileNameList = fileNameByPostIdList
                .stream()
                .filter(fileName -> partitionedImages.get(false)
                        .stream()
                        .noneMatch(imageDTO -> imageDTO.getFileName().equals(fileName)))
                .toList();

        // 새로 추가된 이미지 정보만 담긴 ImageCreateRequestDTO
        List<ImageCreateRequestDTO> newImageCreateRequestDTO = partitionedImages.get(true);

        // 이미지 소프트 딜리트
        if (!deleteFileNameList.isEmpty()) {
            imageRepository.softDeleteByFileNames(deleteFileNameList);
        }

        if (!newImageCreateRequestDTO.isEmpty()) {
            for (ImageCreateRequestDTO newImageDTO : newImageCreateRequestDTO) {
                Image image = Image.builder()
                        .user(user)
                        .fileName(newImageDTO.getFileName())
                        .imageUrl(newImageDTO.getImageUrl())
                        .originalName(newImageDTO.getOriginalFileName())
                        .fileType(newImageDTO.getFileType())
                        .fileSize(newImageDTO.getFileSize())
                        .build();

                imageRepository.save(image);

                post.addCommunityImage(
                        CommunityImage.builder()
                                .image(image)
                                .post(post)
                                .build()
                );
            }
        }

    }

    private Post getPost(PostCreateRequestDTO postCreateRequestDTO, User user, Category category) {
        return Post.builder()
                .user(user)
                .category(category)
                .title(postCreateRequestDTO.getTitle())
                .content(postCreateRequestDTO.getContent())
                .views(0L)
                .likes(0L)
                .build();

    }

    private void validateImageCount(List<ImageCreateRequestDTO> imageList) {
        if (imageList != null && imageList.size() > 5) {
            throw new FileException.FileUploadException("5개 이상 이미지를 등록할 수 없습니다.");
        }
    }

}
