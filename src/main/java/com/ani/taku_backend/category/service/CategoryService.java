package com.ani.taku_backend.category.service;

import com.ani.taku_backend.category.domain.dto.RequestCategoryCreateDTO;
import com.ani.taku_backend.category.domain.dto.ResponseCategoryDTO;
import com.ani.taku_backend.category.domain.entity.Category;
import com.ani.taku_backend.category.domain.entity.CategoryGenre;
import com.ani.taku_backend.category.domain.entity.CategoryImage;
import com.ani.taku_backend.category.domain.repository.AnimationGenreRepository;
import com.ani.taku_backend.category.domain.repository.CategoryGenreRepository;
import com.ani.taku_backend.category.domain.repository.CategoryRepository;
import com.ani.taku_backend.common.annotation.RequireUser;
import com.ani.taku_backend.common.exception.FileException;
import com.ani.taku_backend.common.model.dto.CreateImageDTO;
import com.ani.taku_backend.common.model.entity.Image;
import com.ani.taku_backend.common.service.FileService;
import com.ani.taku_backend.common.service.ImageService;
import com.ani.taku_backend.common.util.FileUtil;
import com.ani.taku_backend.global.exception.CustomException;
import com.ani.taku_backend.global.exception.ErrorCode;
import com.ani.taku_backend.user.model.dto.PrincipalUser;
import com.ani.taku_backend.user.model.entity.BlackUser;
import com.ani.taku_backend.user.model.entity.User;
import com.ani.taku_backend.user.service.BlackUserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;


@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final AnimationGenreRepository animationGenreRepository;
    private final FileService fileService;
    private final ImageService imageService;
    private final BlackUserService blackUserService;
    private final ModelMapper modelMapper;

    /**
     * 카테고리 생성
     * @param principalUser
     * @param requestCategoryCreateDTO
     * @param uploadFile
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @RequireUser
    public ResponseCategoryDTO createCategory(PrincipalUser principalUser, RequestCategoryCreateDTO requestCategoryCreateDTO, MultipartFile uploadFile) {

        // 이미지 확장자 검증 추가
        if(!FileUtil.isImgExtension(uploadFile.getOriginalFilename())){
            throw new CustomException(ErrorCode.INVALIDATE_IMAGE);
        }

        // 블랙리스트 검증을 먼저 수행
        validateBlackUser(principalUser.getUserId());
        
        // 이미지 처리
        Image savedImage = processAndSaveImage(uploadFile, principalUser.getUser());
        
        // 카테고리 생성 및 저장
        Category category = createCategoryWithRelations(requestCategoryCreateDTO, principalUser.getUser(), savedImage);
        Category savedCategory = categoryRepository.save(category);
        
        return modelMapper.map(savedCategory, ResponseCategoryDTO.class);
    }

    /** TODO : AOP로 변경필요
     * 블랙리스트 검증
     * @param userId
     */
    private void validateBlackUser(Long userId) {
        List<BlackUser> blackUser = blackUserService.findByUserId(userId);
        if(blackUser.isEmpty()) {
            throw new CustomException(ErrorCode.BLACK_USER);
        }
    }

    /**
     * 이미지 처리 및 저장
     * @param uploadFile
     * @param user
     * @return
     */
    private Image processAndSaveImage(MultipartFile uploadFile, User user) {
        try {
            CreateImageDTO imageDTO = CreateImageDTO.builder()
                .uploadId(user.getUserId())
                .imageUrl(fileService.uploadFile(uploadFile))
                .fileName(FileUtil.getUuidFileName(uploadFile.getOriginalFilename()))
                .originalFileName(uploadFile.getOriginalFilename())
                .fileType(FileUtil.getExtension(uploadFile.getOriginalFilename()))
                .fileSize((int)uploadFile.getSize())
                .build();

            Image savedImage = imageService.insertImage(Image.of(imageDTO, user));
            return savedImage;
        } catch (IOException e) {
            log.error("이미지 업로드 실패 : {}", e.getMessage());
            throw new CustomException(ErrorCode.INVALIDATE_IMAGE);
        }
    }

    /**
     * 카테고리 생성 및 관계 설정
     * @param dto
     * @param user
     * @param image
     * @return
     */
    private Category createCategoryWithRelations(RequestCategoryCreateDTO dto, User user, Image image) {
        Category category = Category.from(dto, user);
        
        // 카테고리 이미지 관계 설정
        CategoryImage categoryImage = CategoryImage.builder()
            .image(image)
            .category(category)
            .build();
        category.getCategoryImages().add(categoryImage);

        // 카테고리 장르 관계 설정
        Arrays.asList(dto.getAniGenreId()).forEach(genreId -> {
            CategoryGenre categoryGenre = CategoryGenre.builder()
                .category(category)
                .genre(animationGenreRepository.findById(genreId).get())
                .build();
            category.getCategoryGenres().add(categoryGenre);
        });

        return category;
    }
}
