package com.ani.taku_backend.category.service;

import com.ani.taku_backend.category.dto.AniGenreListReqDTO;
import com.ani.taku_backend.category.dto.AniGenreResDTO;
import com.ani.taku_backend.category.dto.CreateCategoryReqDTO;
import com.ani.taku_backend.category.dto.CategorySearchReqDTO;
import com.ani.taku_backend.category.dto.CategoryResDTO;
import com.ani.taku_backend.category.dto.CategorySeachResDTO;
import com.ani.taku_backend.category.domain.entity.Category;
import com.ani.taku_backend.category.domain.entity.CategoryGenre;
import com.ani.taku_backend.category.domain.entity.CategoryImage;
import com.ani.taku_backend.category.domain.repository.AnimationGenreRepository;
import com.ani.taku_backend.category.domain.repository.CategoryRepository;
import com.ani.taku_backend.category_bookmark.domain.repository.CategoryBookmarkRepository;
import com.ani.taku_backend.common.aop.annotation.RequireUser;
import com.ani.taku_backend.common.exception.DuckwhoException;
import com.ani.taku_backend.common.exception.ErrorCode;
import com.ani.taku_backend.common.model.dto.CreateImageDTO;
import com.ani.taku_backend.common.model.entity.Image;
import com.ani.taku_backend.common.remote_file.ImageService;
import com.ani.taku_backend.common.remote_file.RemoteFileService;
import com.ani.taku_backend.common.remote_file.RemoteFileServiceFactory;
import com.ani.taku_backend.common.util.FileUtil;
import com.ani.taku_backend.common.util.KoreanUtil;
import com.ani.taku_backend.common.util.StringSimilarity;
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

import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryBookmarkRepository categoryBookmarkRepository;
    private final AnimationGenreRepository animationGenreRepository;
    private final ImageService imageService;
    private final BlackUserService blackUserService;
    private final ModelMapper modelMapper;
    private final RemoteFileServiceFactory remoteFileServiceFactory;

    /**
     * 카테고리 생성
     *
     * @param user
     * @param createCategoryReqDTO
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @RequireUser
    public CategoryResDTO createCategory(User user, CreateCategoryReqDTO createCategoryReqDTO) throws DuckwhoException {
        MultipartFile uploadFile = createCategoryReqDTO.getImage();

        // 이미지 확장자 검증 추가
        if(!FileUtil.isImgExtension(uploadFile.getOriginalFilename())){
            throw new DuckwhoException(ErrorCode.INVALID_FILE_FORMAT);
        }

        // 카테고리 이름 검증
        validateCategoryName(createCategoryReqDTO.getName());
        
        // 이미지 처리
        String contentType = uploadFile.getContentType();
        RemoteFileService remoteFileService = remoteFileServiceFactory.getService(contentType);
        String uploadedFileUrl = remoteFileService.uploadFile(uploadFile);

        CreateImageDTO imageDTO = CreateImageDTO.builder()
                .uploadId(user.getUserId())
                .imageUrl(uploadedFileUrl)
                .fileName(FileUtil.getUuidFileName(uploadFile.getOriginalFilename()))
                .originalFileName(uploadFile.getOriginalFilename())
                .fileType(FileUtil.getExtension(uploadFile.getOriginalFilename()))
                .fileSize((int)uploadFile.getSize())
                .build();
        Image savedImage = imageService.insertImage(Image.of(imageDTO, user));

        // 카테고리 생성 및 저장
        Category category = createCategoryWithRelations(createCategoryReqDTO, user, savedImage);
        Category savedCategory = categoryRepository.save(category);
        
        return modelMapper.map(savedCategory, CategoryResDTO.class);
    }


    /**
     * 카테고리 검색
     * @param pageable
     * @return
     */
    public Page<CategorySeachResDTO> searchCategories(CategorySearchReqDTO categorySearchReqDTO, Pageable pageable) {
        return categoryRepository.searchCategories(categorySearchReqDTO, pageable);
    }

    /**
     * 카테고리 상세 조회
     * @param id
     * @param user
     * @return
     */
    public CategoryResDTO findCategoryById(Long id, User user) {
        Category category = categoryRepository.findCategoryById(id, user)
                .orElseThrow(()-> new DuckwhoException(ErrorCode.NOT_FOUND_CATEGORY));

        boolean hasBookmark = Optional.ofNullable(user)
                .map(u -> categoryBookmarkRepository.findByCategoryIdAndUserUserId(id, u.getUserId()))
                .map(Optional::isPresent)
                .orElse(false);

        return CategoryResDTO.of(category, hasBookmark);
    }

    @Override
    public AniGenreListReqDTO findAniGenres(String keyword) {
        List<AniGenreResDTO> aniGenres = animationGenreRepository.findByGenreName(keyword);
        return AniGenreListReqDTO.builder()
                .genres(aniGenres)
                .build();
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

    /**
     * 카테고리 생성 및 관계 설정
     * @param dto
     * @param user
     * @param image
     * @return
     */
    private Category createCategoryWithRelations(CreateCategoryReqDTO dto, User user, Image image) {
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
