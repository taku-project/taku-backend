package com.ani.taku_backend.jangter.service;

import com.ani.taku_backend.common.annotation.CheckViewCount;
import com.ani.taku_backend.common.annotation.RequireUser;
import com.ani.taku_backend.common.annotation.ValidateProfanity;
import com.ani.taku_backend.common.enums.StatusType;
import com.ani.taku_backend.common.enums.ViewType;
import com.ani.taku_backend.common.exception.DuckwhoException;
import com.ani.taku_backend.common.model.entity.Image;
import com.ani.taku_backend.common.service.ExtractKeywordService;
import com.ani.taku_backend.common.service.FileService;
import com.ani.taku_backend.common.service.ImageService;
import com.ani.taku_backend.jangter.model.dto.ProductCreateRequestDTO;
import com.ani.taku_backend.jangter.model.dto.ProductFindDetailResponseDTO;
import com.ani.taku_backend.jangter.model.dto.ProductUpdateRequestDTO;
import com.ani.taku_backend.jangter.model.entity.DuckuJangter;
import com.ani.taku_backend.jangter.model.entity.ItemCategories;
import com.ani.taku_backend.jangter.model.entity.JangterImages;
import com.ani.taku_backend.jangter.repository.DuckuJangterRepository;
import com.ani.taku_backend.jangter.repository.ItemCategoriesRepository;
import com.ani.taku_backend.user.model.dto.PrincipalUser;
import com.ani.taku_backend.user.model.entity.BlackUser;
import com.ani.taku_backend.user.model.entity.User;
import com.ani.taku_backend.user.service.BlackUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

import static com.ani.taku_backend.common.exception.ErrorCode.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DuckuJangterService {

    private final DuckuJangterRepository duckuJangterRepository;
    private final ItemCategoriesRepository itemCategoriesRepository;
    private final ImageService imageService;
    private final BlackUserService blackUserService;
    private final FileService fileService;
    private final ExtractKeywordService extractKeywordService;
    /**
     * 장터글 저장
     */
    @Transactional
    @RequireUser
    @ValidateProfanity(fields = {"title", "description"})  // 금칙어 적용 완료
    public Long createProduct(ProductCreateRequestDTO productCreateRequestDTO,
                              List<MultipartFile> imageList,
                              PrincipalUser principalUser) {
        // 블랙유저 검증
        User user = validateBlockUser(principalUser);

        ItemCategories findItemCategory = getItemCategories(productCreateRequestDTO.getCategoryId(), null);

        // 이미지 저장 - r2, rdb 모두 저장
        List<Image> saveImageList = imageService.saveImageList(imageList, user);

        // 엔티티 생성
        DuckuJangter product = createProduct(productCreateRequestDTO, user, findItemCategory);

        // jangerImages 연관관계 설정
        setRelationJangterImages(saveImageList, product);

        Long saveProductId = duckuJangterRepository.save(product).getId();
        log.info("장터 판매글 등록 완료, 게시글 Id: {}", saveProductId);

        return saveProductId;
    }

    /**
     * 장터글 상세 조회
     */
    @CheckViewCount(viewType = ViewType.SHOP,
            targetId = "productId",
            expireTime = 60)
    public ProductFindDetailResponseDTO findProductDetail(Long productId, boolean isFirstView) {

        // 판매글 조회
        DuckuJangter findProductDetail = duckuJangterRepository.findById(productId)
                .orElseThrow(() -> new DuckwhoException(NOT_FOUND_POST));

        validateDelete(findProductDetail);

        // 조회수 증가 로직
        long addViewCount = findProductDetail.addViewCount(isFirstView);

        log.info("장터글 조회 완료, 장터글 상세: {}", findProductDetail);

        return new ProductFindDetailResponseDTO(findProductDetail, findProductDetail.getStatus(), addViewCount);
    }

    /**
     * 게시글 업데이트
     */
    @Transactional
    @RequireUser
    @ValidateProfanity(fields = {"title", "description"})
    public Long updateProduct(Long productId,
                              ProductUpdateRequestDTO productUpdateRequestDTO,
                              List<MultipartFile> imageList,
                              PrincipalUser principalUser) {

        // 게시글 조회
        DuckuJangter findProduct = duckuJangterRepository.findById(productId)
                .orElseThrow(() -> new DuckwhoException(NOT_FOUND_POST));

        validateDelete(findProduct);

        // 블랙 유저인지 검증
        User user = validateBlockUser(principalUser);

        if (!user.getUserId().equals(findProduct.getUser().getUserId())) {  // 본인 글인지 확인
            throw new DuckwhoException(UNAUTHORIZED_ACCESS);
        }

        // 카테고리 가져오기
        ItemCategories itemCategories = getItemCategories(productUpdateRequestDTO.getCategoryId(), null);

        // 업데이트 이미지 저장
        List<Image> newImageList = imageService.getUpdateImageList(productUpdateRequestDTO, imageList, findProduct, user);

        if (!newImageList.isEmpty()) {
            setRelationJangterImages(newImageList, findProduct);   // 연관관계 설정
        }

        // 장터글 업데이트
        findProduct.updateProduct(productUpdateRequestDTO, itemCategories);

        log.info("장터글 업데이트 완료, 글 상세 {}", findProduct);
        return findProduct.getId();
    }

    /**
     * 장터글 삭제
     */
    @Transactional
    @RequireUser
    public void deleteProduct(long productId, Long categoryId, PrincipalUser principalUser) {

        User user = validateBlockUser(principalUser);

        DuckuJangter findProduct = duckuJangterRepository.findById(productId)
                .orElseThrow(() -> new DuckwhoException(NOT_FOUND_POST));

        getItemCategories(categoryId, findProduct);

        if (!user.getUserId().equals(findProduct.getUser().getUserId())) {
            throw new DuckwhoException(UNAUTHORIZED_ACCESS);
        }

        findProduct.delete();  // 장터 에서 소프트 딜리트
        // 장터 이미지에서 이미지를 조회해서 장터와 연관된 이미지들을 모두 softDelete, 클라우드 플레어에서도 삭제
        findProduct.getJangterImages().forEach(jangterImages -> {
            jangterImages.getImage().delete();
            fileService.deleteImageFile(jangterImages.getImage().getFileName());
        });
        log.info("장터글 삭제 완료 - 삭제일: {}", findProduct.getDeletedAt());
    }

    private void validateDelete(DuckuJangter findProductDetail) {
        // 삭제된 글이면 예외
        if (findProductDetail.getDeletedAt() != null) {
            throw new DuckwhoException(NOT_FOUND_POST);
        }
    }

    // 장터이미지 연관관계설정
    private void setRelationJangterImages(List<Image> saveImageList, DuckuJangter product) {
        for (Image image : saveImageList) {
            JangterImages jangterImage = JangterImages.builder()
                    .duckuJangter(product)
                    .image(image)
                    .build();
            product.addJangterImage(jangterImage);
        }
    }

    // 장터글 엔티티 생성
    private DuckuJangter createProduct(ProductCreateRequestDTO productCreateRequestDTO, User user, ItemCategories findItemCategory) {
        return DuckuJangter.builder()
                .user(user)
                .itemCategories(findItemCategory)
                .title(productCreateRequestDTO.getTitle())
                .description(productCreateRequestDTO.getDescription())
                .price(productCreateRequestDTO.getPrice())
                .status(StatusType.ACTIVE)
                .viewCount(0L)
                .build();
    }

    // 블랙리스트 검증
    private User validateBlockUser(PrincipalUser principalUser) {
        User user = principalUser.getUser();
        List<BlackUser> byUserId = blackUserService.findByUserId(user.getUserId());
        if (!byUserId.isEmpty() && byUserId.get(0).getId().equals(user.getUserId())) {
            log.info("블랙유저 {}", user);
            throw new DuckwhoException(UNAUTHORIZED_ACCESS);
        }
        log.info("일반 유저 {}", user);
        return user;
    }

    // 카테고리 검증
    private ItemCategories getItemCategories(long categoryId, DuckuJangter findProduct) {
        ItemCategories itemCategories = itemCategoriesRepository.findById(categoryId)
                .orElseThrow(() -> new DuckwhoException(NOT_FOUND_CATEGORY));
        log.info("아이템 카테고리: {} ", itemCategories.getName());

        if (findProduct != null && !findProduct.getItemCategories().getId().equals(itemCategories.getId())) {
            throw new DuckwhoException(UNAUTHORIZED_ACCESS);
        }

        return itemCategories;
    }


    @RequireUser
    public void recommendProduct(Long productId, PrincipalUser principalUser) {

        // 상품상세
        final DuckuJangter product = this.duckuJangterRepository.findById(productId)
                .orElseThrow(() -> new DuckwhoException(NOT_FOUND_POST));

        
        String title = product.getTitle();
        BigDecimal price = product.getPrice();
        BigDecimal priceRangePercentage = new BigDecimal("0.20"); // 20%
        BigDecimal minPrice = price.subtract(price.multiply(priceRangePercentage));
        BigDecimal maxPrice = price.add(price.multiply(priceRangePercentage));


        // TODO : 병렬처리

        log.info("title : {}, price : {}, minPrice : {}, maxPrice : {}", title, price, minPrice, maxPrice);

        List<String> keywords = this.extractKeywordService.extractKeywords(title);

        log.info("keywords : {}", keywords);

        // 같은 카테고리의 상품 조회
        Long itemCategoryId = product.getItemCategories().getId();
        List<DuckuJangter> recommendProducts = this.duckuJangterRepository
            .findRecommendFilteredProducts(keywords, minPrice, maxPrice, itemCategoryId, StatusType.ACTIVE);

        log.info("1 step >> recommendProducts : {}", recommendProducts);

        Long userId = principalUser.getUserId();
        List<DuckuJangter> buyUserProducts = this.duckuJangterRepository.findByBuyUserId(userId);

        log.info("user buy product >> buyUserProducts : {}", buyUserProducts);

        // 몽고디비 검색이력 조회


        // 사용자 찜목록 조회


        // 데이터가지고 점수 계산

        // 점수 순으로 정렬

        // 상위 5개 추천

        //TODO: 5개가 없으면? 같은 카테고리의 상품에서 랜덤? 추천

        // 랜덤추천할 상품도 없으면? 모든 상품에서 랜덤 추천
    }
}
