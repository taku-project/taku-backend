package com.ani.taku_backend.category.service;

import com.ani.taku_backend.category.domain.dto.RequestCategoryCreateDTO;
import com.ani.taku_backend.category.domain.dto.RequestCategorySearch;
import com.ani.taku_backend.category.domain.dto.ResponseCategoryDTO;
import com.ani.taku_backend.category.domain.dto.ResponseCategorySeachDTO;
import com.ani.taku_backend.category.domain.entity.Category;
import com.ani.taku_backend.category.domain.entity.CategoryGenre;
import com.ani.taku_backend.category.domain.entity.CategoryImage;
import com.ani.taku_backend.category.domain.repository.AnimationGenreRepository;
import com.ani.taku_backend.category.domain.repository.CategoryRepository;
import com.ani.taku_backend.common.annotation.RequireUser;
import com.ani.taku_backend.common.exception.DuckwhoException;
import com.ani.taku_backend.common.exception.ErrorCode;
import com.ani.taku_backend.common.model.dto.CreateImageDTO;
import com.ani.taku_backend.common.model.entity.Image;
import com.ani.taku_backend.common.service.FileService;
import com.ani.taku_backend.common.service.ImageService;
import com.ani.taku_backend.common.util.FileUtil;
import com.ani.taku_backend.common.util.KoreanUtil;
import com.ani.taku_backend.common.util.StringSimilarity;
import com.ani.taku_backend.user.model.dto.PrincipalUser;
import com.ani.taku_backend.user.model.entity.BlackUser;
import com.ani.taku_backend.user.model.entity.User;
import com.ani.taku_backend.user.service.BlackUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryServiceImpl implements CategoryService {
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
    public ResponseCategoryDTO createCategory(PrincipalUser principalUser, RequestCategoryCreateDTO requestCategoryCreateDTO, MultipartFile uploadFile) throws DuckwhoException {

        // 이미지 확장자 검증 추가
        if(!FileUtil.isImgExtension(uploadFile.getOriginalFilename())){
            throw new DuckwhoException(ErrorCode.INVALID_FILE_FORMAT);
        }

        // 카테고리 이름 검증
        validateCategoryName(requestCategoryCreateDTO.getName());

        // 블랙리스트 검증을 먼저 수행
        validateBlackUser(principalUser.getUserId());
        
        // 이미지 처리
        Image savedImage = processAndSaveImage(uploadFile, principalUser.getUser());
        
        // 카테고리 생성 및 저장
        Category category = createCategoryWithRelations(requestCategoryCreateDTO, principalUser.getUser(), savedImage);
        Category savedCategory = categoryRepository.save(category);
        
        return modelMapper.map(savedCategory, ResponseCategoryDTO.class);
    }


    /**
     * 카테고리 검색
     * @param pageable
     * @return
     */
    public Page<ResponseCategorySeachDTO> searchCategories(RequestCategorySearch requestCategorySearch, Pageable pageable) {
        return categoryRepository.searchCategories(requestCategorySearch, pageable);
    }

    /**
     * 카테고리 상세 조회
     * @param id
     * @return
     */
    public ResponseCategoryDTO findCategoryById(Long id) {
        Optional<Category> categoryOptional = categoryRepository.findById(id);

        if(!categoryOptional.isPresent()) {
            throw new DuckwhoException(ErrorCode.NOT_FOUND_CATEGORY);
        }
        return modelMapper.map(categoryOptional.get(), ResponseCategoryDTO.class);
    }

    /**
     * 카테고리 이름 검증
     * @param newCategoryName
     */
    private void validateCategoryName(String newCategoryName) {
        // 원본 이름에서 공백 제거 및 소문자 변환
        String searchName = newCategoryName.replaceAll("\\s+", "").toLowerCase();
        
        // 초성 추출 (예: "나루토" -> "ㄴㄹㅌ")
        String chosung = KoreanUtil.getChosung(searchName);
        
        // 유사 이름 검색
        List<Category> similarCategories = categoryRepository.findSimilarNames(
            chosung,            // 초성
            searchName,         // 공백 제거된 검색어
            newCategoryName     // 원본 이름
        );
        
        // 추출된 후보군에 대해서만 레벤슈타인 거리 계산
        for (Category category : similarCategories) {
            // 원본 이름 비교
            double similarity = StringSimilarity.calculateSimilarity(
                searchName,
                category.getName().replaceAll("\\s+", "").toLowerCase()
            );
            
            if (similarity >= StringSimilarity.SIMILARITY_THRESHOLD) {
                throw new DuckwhoException(ErrorCode.DUPLICATE_CATEGORY_NAME);
            }
        }
    }

    /** TODO : AOP로 변경필요
     * 블랙리스트 검증
     * @param userId
     */
    private void validateBlackUser(Long userId) {
        List<BlackUser> blackUser = blackUserService.findByUserId(userId);
        if(!blackUser.isEmpty()) {
            throw new DuckwhoException(ErrorCode.BLACK_USER);
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
                .imageUrl(fileService.uploadVideoFile(uploadFile))
                .fileName(FileUtil.getUuidFileName(uploadFile.getOriginalFilename()))
                .originalFileName(uploadFile.getOriginalFilename())
                .fileType(FileUtil.getExtension(uploadFile.getOriginalFilename()))
                .fileSize((int)uploadFile.getSize())
                .build();

            Image savedImage = imageService.insertImage(Image.of(imageDTO, user));
            return savedImage;
        } catch (IOException e) {
            log.error("이미지 업로드 실패 : {}", e.getMessage());
            throw new DuckwhoException(ErrorCode.FILE_UPLOAD_ERROR);
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

        category.setCategoryImage(categoryImage);

        // 카테고리 장르 관계 설정
        dto.getAniGenreId().forEach(genreId -> {
            CategoryGenre categoryGenre = CategoryGenre.builder()
                .category(category)
                .genre(animationGenreRepository.findById(genreId).orElseThrow(() -> new DuckwhoException(ErrorCode.NOT_FOUND_GENRE)))
                .build();
            category.getCategoryGenres().add(categoryGenre);
        });

        return category;
    }
}
