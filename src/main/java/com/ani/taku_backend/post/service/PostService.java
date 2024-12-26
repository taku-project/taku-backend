package com.ani.taku_backend.post.service;

import com.ani.taku_backend.category.domain.entity.Category;
import com.ani.taku_backend.category.domain.repository.CategoryRepository;
import com.ani.taku_backend.common.annotation.RequireUser;
import com.ani.taku_backend.common.exception.FileException;
import com.ani.taku_backend.common.exception.PostException;
import com.ani.taku_backend.common.model.dto.ImageCreateRequestDTO;
import com.ani.taku_backend.common.model.entity.Image;
import com.ani.taku_backend.common.repository.ImageRepository;
import com.ani.taku_backend.common.service.FileService;
import com.ani.taku_backend.post.model.dto.PostCreateUpdateRequestDTO;
import com.ani.taku_backend.post.model.dto.PostListResponseDTO;
import com.ani.taku_backend.post.model.entity.CommunityImage;
import com.ani.taku_backend.post.model.entity.Post;
import com.ani.taku_backend.post.repository.PostRepository;
import com.ani.taku_backend.user.model.dto.PrincipalUser;
import com.ani.taku_backend.user.model.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final CategoryRepository categoryRepository;
    private final ImageRepository imageRepository;
    private final FileService fileService;

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
    public Long createPost(PostCreateUpdateRequestDTO postCreateRequestDTO, PrincipalUser principalUser, List<MultipartFile> imageList) {
        // 유저 정보 가져오기
        User user = principalUser.getUser();

        // 카테고리 확인
        Category category = categoryRepository.findById(postCreateRequestDTO.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("카테고리를 찾을 수 없습니다. ID: " + postCreateRequestDTO.getCategoryId()));

        // 게시글 생성 및 저장
        Post post = getPost(postCreateRequestDTO, user, category);
        postRepository.save(post);

        // 이미지 생성 및 저장
        for (MultipartFile image : imageList) {
            try {
                String imageUrl = fileService.uploadFile(image);
                if ((postCreateRequestDTO.getImagelist() != null) && !postCreateRequestDTO.getImagelist().isEmpty()) {
                    validateImageCount(postCreateRequestDTO.getImagelist());    // 5개 이상이면 예외 발생
                    saveImage(postCreateRequestDTO, user, post, imageUrl);
                }
            } catch (IOException e) {
                throw new FileException("파일 업로드에 실패 하였습니다");
            }
        }

        return post.getId();
    }

    /**
     * 게시글 업데이트
     */
    @RequireUser
    @Transactional
    public Long updatePost(PostCreateUpdateRequestDTO postUpdateRequestDTO, PrincipalUser principalUser, Long postId, List<MultipartFile> imageList) {
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
        for (MultipartFile image : imageList) {
            try {
                String imageUrl = fileService.uploadFile(image);
                if ((postUpdateRequestDTO.getImagelist() != null) && !postUpdateRequestDTO.getImagelist().isEmpty()) {
                    validateImageCount(postUpdateRequestDTO.getImagelist());    // 5개 이상이면 예외 발생
                    updateImage(postUpdateRequestDTO, user, post, imageUrl);
                }
            } catch (IOException e) {
                throw new FileException("파일 업로드에 실패 하였습니다");
            }
        }

        // 게시글 수정
        post.updatePost(postUpdateRequestDTO.getTitle(), postUpdateRequestDTO.getContent(), newCategory);

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

    private void saveImage(PostCreateUpdateRequestDTO postCreateRequestDTO, User user, Post post, String imageUrl) {
        String fileName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
        for (ImageCreateRequestDTO getImage : postCreateRequestDTO.getImagelist()) {
            Image image = Image.builder()
                    .user(user)
                    .fileName(fileName)
                    .imageUrl(imageUrl)
                    .originalName(getImage.getOriginalFileName())
                    .fileType(getImage.getFileType())
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

    private void updateImage(PostCreateUpdateRequestDTO postUpdateRequestDTO, User user, Post post, String imageUrl) {
        String fileName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);

        List<String> fileNameByPostIdList = imageRepository.findFileNamesByPostId(post.getId());

        // 기존 게시글에 저장된 이미지와 요청으로 들어온 이미지의 파일네임을 비교하여 같은것과 다른것을 분리
        Map<Boolean, List<ImageCreateRequestDTO>> partitionedImages = postUpdateRequestDTO.getImagelist()
                .stream()
                .collect(Collectors.partitioningBy(
                        imageDTO -> fileNameByPostIdList.contains(fileName)));

        // 이미지 삭제 대상: 기존 게시글에 저장된 이미지와 분리된 파티션의 fasle인 대상중에서 요청으로 넘어온 대상을 제외한 대상
        List<String> deleteFileNameList = fileNameByPostIdList
                .stream()
                .filter(deleteFileName -> partitionedImages.get(false)
                        .stream()
                        .noneMatch(imageDTO -> fileName.equals(deleteFileName)))
                .toList();

        // 새로 추가된 이미지 정보만 담긴 ImageCreateRequestDTO
        List<ImageCreateRequestDTO> newImageCreateRequestDTO = partitionedImages.get(true);

        // 이미지 소프트 딜리트
        if (!deleteFileNameList.isEmpty()) {
            imageRepository.softDeleteByFileNames(deleteFileNameList);
        }

        // 새로 추가될 이미지 정보가 있으면 저장
        if (!newImageCreateRequestDTO.isEmpty()) {
            for (ImageCreateRequestDTO newImageDTO : newImageCreateRequestDTO) {
                Image image = Image.builder()
                        .user(user)
                        .fileName(fileName)
                        .imageUrl(imageUrl)
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

    private Post getPost(PostCreateUpdateRequestDTO postCreateRequestDTO, User user, Category category) {
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
