package com.ani.taku_backend.jangter.service;

import com.ani.taku_backend.common.annotation.RequireUser;
import com.ani.taku_backend.common.enums.StatusType;
import com.ani.taku_backend.common.exception.DuckwhoException;
import com.ani.taku_backend.common.exception.FileException;
import com.ani.taku_backend.common.exception.PostException;
import com.ani.taku_backend.common.model.entity.Image;
import com.ani.taku_backend.common.repository.ImageRepository;
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
import com.ani.taku_backend.jangter.service.viewcount.ViewCountService;
import com.ani.taku_backend.post.model.entity.Post;
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
import java.util.ArrayList;
import java.util.List;

import static com.ani.taku_backend.common.exception.ErrorCode.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DuckuJangterService {

    private final DuckuJangterRepository duckuJangterRepository;
    private final ItemCategoriesRepository itemCategoriesRepository;
    private final ViewCountService viewCountService;
    private final ImageService imageService;
    private final BlackUserService blackUserService;
    private final FileService fileService;
    private final ImageRepository imageRepository;
    private final ExtractKeywordService extractKeywordService;

    /**
     * 장터글 저장
     */
    @Transactional
    @RequireUser
    public Long createProduct(ProductCreateRequestDTO productCreateRequestDTO,
                              List<MultipartFile> imageList,
                              PrincipalUser principalUser) {
        // 유저 조회 -> 검증 로직 필요, 본인인가?, 관리자도 접속가능, 블랙유저인가,
        User user = principalUser.getUser();

        // 금칙어 aop 적용해야함 -> 댓글 작업까지 하고진행

        ItemCategories findItemCategory = getItemCategories(productCreateRequestDTO.getCategoryId());

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
                .itemCategory(findItemCategory)
                .title(productCreateRequestDTO.getTitle())
                .description(productCreateRequestDTO.getDescription())
                .price(productCreateRequestDTO.getPrice())
                .status(StatusType.ACTIVE)
                .viewCount(0L)
                .build();
    }

    /**
     * 장터글 상세 조회
     */
    @Transactional
    public ProductFindDetailResponseDTO findProductDetail(Long productId) {

        // 판매글 조회
        DuckuJangter findProductDetail = duckuJangterRepository.findById(productId)
                .orElseThrow(() -> new DuckwhoException(NOT_FOUND_POST));

        // 조회 수 증가 및 조회수 가져오기
        viewCountService.incrementViewCount(productId);
        Long viewCount = viewCountService.getViewCount(productId);

        return new ProductFindDetailResponseDTO(findProductDetail, findProductDetail.getStatus(), viewCount);
    }

    /**
     * 게시글 업데이트
     */
    @Transactional
    @RequireUser
    public Long updateProduct(Long productId, ProductUpdateRequestDTO productUpdateRequestDTO, List<MultipartFile> imageList, PrincipalUser principalUser) {
        // 게시글 조회
        DuckuJangter findProduct = duckuJangterRepository.findById(productId).orElseThrow(
                () -> new DuckwhoException(NOT_FOUND_POST));

        // 유저 조회 검증 -> 블랙유저 검증 필요 -> 이건 따로 메서드 추출해서 전체로 적용해보자. 리펠토링할 때
        User user = principalUser.getUser();
        List<BlackUser> byUserId = blackUserService.findByUserId(user.getUserId());

        if (!user.getNickname().equals(findProduct.getUser().getNickname())) {  // 본인 글인지 확인
            throw new DuckwhoException(UNAUTHORIZED_ACCESS);
        }

        // 마찬가지로.. 금칙어 적용

        // 카테고리 가져오기
        ItemCategories itemCategories = getItemCategories(productUpdateRequestDTO.getCategoryId());

        List<MultipartFile> newImageList = imageService.getUpdateImageList(productId, productUpdateRequestDTO, imageList, findProduct);

        // 업데이트 이미지 저장
        if (!newImageList.isEmpty()) {
            List<Image> saveImageList = imageService.saveImageList(newImageList, user); // 이미지 저장
            setRelationJangterImages(saveImageList, findProduct);   // 연관관계 설정
        }

        // 게시글 업데이트
        findProduct.updateDuckuJangter(productUpdateRequestDTO.getTitle(),
                                        productUpdateRequestDTO.getDescription(),
                                        productUpdateRequestDTO.getPrice(), itemCategories);

        return findProduct.getId();
    }


    private ItemCategories getItemCategories(Long categoryId) {
        return itemCategoriesRepository.findById(categoryId)
                .orElseThrow(() -> new DuckwhoException(NOT_FOUND_CATEGORY));
    }

    /**
     * 장터글 삭제 -> 일단은 반환값 없이 진행 하구, 클라이언트와 대화를 통해.. 반환값을 어떻게 할지 정해보기
     */
    @Transactional
    @RequireUser
    public void deleteProduct(long productId, PrincipalUser principalUser) {
        User user = principalUser.getUser();
        DuckuJangter duckuJangter = duckuJangterRepository.findById(productId)
                .orElseThrow(() -> new DuckwhoException(NOT_FOUND_POST));

        if (!user.getUserId().equals(duckuJangter.getUser().getUserId())) {
            throw new DuckwhoException(UNAUTHORIZED_ACCESS);
        }

        duckuJangter.softDelete();  // 장터 에서 소프트 딜리트
        // 장터 이미지에서 이미지를 조회해서 장터와 연관된 이미지들을 모두 softDelete, 클라우드 플레어에서도 삭제
        duckuJangter.getJangterImages().forEach(jangterImages -> {
            jangterImages.getImage().softDelete();
            fileService.deleteFile(jangterImages.getImage().getFileName());
        });
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
        Long itemCategoryId = product.getItemCategory().getId();
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
