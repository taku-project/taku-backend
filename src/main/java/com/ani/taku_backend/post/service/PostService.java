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
        if (imageList != null && !imageList.isEmpty()) {
            saveImages(postCreateRequestDTO, imageList, user, post);
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
        post.softDelete();
        post.getCommunityImages().forEach(communityImage -> {
            communityImage.getImage().delete();
            post.removeCommunityImage(communityImage);
        });

        return post.getId();
    }

    @Transactional
    protected void saveImages(PostCreateUpdateRequestDTO postCreateRequestDTO, List<MultipartFile> imageList, User user, Post post) {
        for (MultipartFile image : imageList) {
            try {
                String imageUrl = fileService.uploadVideoFile(image);
                if ((postCreateRequestDTO.getImagelist() != null) && !postCreateRequestDTO.getImagelist().isEmpty()) {
                    validateImageCount(postCreateRequestDTO.getImagelist());    // 5개 이상이면 예외 발생
                }
            } catch (IOException e) {
                throw new FileException("파일 업로드에 실패 하였습니다");
            }
        }
    }

    @Transactional
    protected void updateImages(PostCreateUpdateRequestDTO requestDTO, List<MultipartFile> imageList, User user, Post post) {
        if (requestDTO.getImagelist() != null) {
            validateImageCount(requestDTO.getImagelist());
        }

        // 게시글에서 첨부파일을 모두 삭제하고 넘어옴
        if (imageList == null || imageList.isEmpty()) {
            post.getCommunityImages().forEach(communityImage -> {
                Image image = communityImage.getImage();
                image.delete(); // Soft delete 호출
            });
            return;
        }

        /**
         * 수정 필요함 잘못짰음 -> DB에 저장된 파일은 UUID값, MultipartFile에서 가져오는 파일은 실제 파일이름, 비교가 안됨
         */
        List<String> existingFileNames = imageRepository.findFileNamesByPostId(post.getId());   // 파일이름 추출
        List<String> newFileNames = imageList.stream().map(MultipartFile::getOriginalFilename).toList();    // 새로 저장할 파일이름

        // 기존 파일 삭제 대상 -> 마찬가지로 검증이 안됨 다시 짜야함
        List<String> filesToDelete = existingFileNames.stream()
                .filter(existing -> !newFileNames.contains(existing))
                .toList();

        // 기존 파일 소프트 삭제
        if (!filesToDelete.isEmpty()) {
            imageRepository.softDeleteByFileNames(filesToDelete);
        }

        // 새로 추가된 파일 필터링 (기존 파일과 중복되지 않은 파일만 선택)
        List<MultipartFile> filesToAdd = imageList.stream()
                .filter(file -> !existingFileNames.contains(file.getOriginalFilename()))
                .toList();

        // 새 파일 업로드 및 저장
        for (MultipartFile image : filesToAdd) {
            try {
                String imageUrl = fileService.uploadVideoFile(image);
                processImage(requestDTO, user, post, imageUrl);
            } catch (IOException e) {
                throw new FileException("파일 업로드에 실패하였습니다.");
            }
        }
    }

    @Transactional
    void processImage(PostCreateUpdateRequestDTO requestDTO, User user, Post post, String imageUrl) {
        String fileName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);

        for (ImageCreateRequestDTO imageDTO : requestDTO.getImagelist()) {
            Image image = Image.builder()
                    .user(user)
                    .fileName(fileName)
                    .imageUrl(imageUrl)
                    .originalName(imageDTO.getOriginalFileName())
                    .fileType(imageDTO.getFileType())
                    .fileSize(imageDTO.getFileSize())
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
