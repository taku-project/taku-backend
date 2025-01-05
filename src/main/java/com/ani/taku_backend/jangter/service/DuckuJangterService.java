package com.ani.taku_backend.jangter.service;

import com.ani.taku_backend.common.annotation.RequireUser;
import com.ani.taku_backend.common.annotation.ValidateProfanity;
import com.ani.taku_backend.common.enums.StatusType;
import com.ani.taku_backend.common.exception.DuckwhoException;
import com.ani.taku_backend.common.model.entity.Image;
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
import com.ani.taku_backend.user.model.entity.User;
import com.ani.taku_backend.user.service.BlackUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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

    /**
     * 장터글 저장
     */
    @Transactional
    @RequireUser
    @ValidateProfanity(fields = {"title", "description"})  // 금칙어 적용 완료
    public Long createProduct(ProductCreateRequestDTO productCreateRequestDTO,
                              List<MultipartFile> imageList,
                              PrincipalUser principalUser) {

        User user = blackUserService.checkBlackUser(principalUser);         // 블랙유저 검증
        ItemCategories itemCategory = checkItemCategory(productCreateRequestDTO.getCategoryId(), null); // 아이템 카테고리 검증

        List<Image> saveImageList = imageService.saveImageList(imageList, user);                // 이미지 저장 - r2, rdb 모두 저장
        DuckuJangter product = createProduct(productCreateRequestDTO, user, itemCategory);      // 엔티티 생성
        setRelationJangterImages(saveImageList, product);                                       // jangerImages 연관관계 설정

        Long saveProductId = duckuJangterRepository.save(product).getId();
        log.info("장터 판매글 등록 완료, 게시글 Id: {}", saveProductId);

        return saveProductId;
    }

    /**
     * 장터글 상세 조회
     */
    public ProductFindDetailResponseDTO findProductDetail(Long productId) {

        // 판매글 조회
        DuckuJangter findProductDetail = duckuJangterRepository.findById(productId)
                .orElseThrow(() -> new DuckwhoException(NOT_FOUND_POST));

        checkDeleteProduct(findProductDetail);

        // 조회 수 증가 -> 나중에 중복 방지 AOP 적용, (진호님 개발된 AOP 적용 해야함)
        long viewCount = findProductDetail.addViewCount();
        log.info("장터글 조회 완료, 장터글 상세: {}", findProductDetail);

        return new ProductFindDetailResponseDTO(findProductDetail, findProductDetail.getStatus(), viewCount);
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

        checkDeleteProduct(findProduct);    //  삭제 검증
        User user = blackUserService.checkBlackUser(principalUser);        // 블랙 유저인지 검증

        if (!user.getUserId().equals(findProduct.getUser().getUserId())) {  // 본인 글인지 확인
            throw new DuckwhoException(UNAUTHORIZED_ACCESS);
        }

        ItemCategories itemCategories = checkItemCategory(productUpdateRequestDTO.getCategoryId(), null);  // 카테고리 검증

        // 업데이트 이미지 저장
        List<Image> newImageList = imageService.getUpdateImageList(productUpdateRequestDTO, imageList, findProduct, user);

        if (!newImageList.isEmpty()) {
            setRelationJangterImages(newImageList, findProduct);   // 연관관계 설정
        }

        findProduct.updateProduct(productUpdateRequestDTO, itemCategories);         // 장터글 업데이트

        log.info("장터글 업데이트 완료, 글 상세 {}", findProduct);
        return findProduct.getId();
    }

    /**
     * 장터글 삭제
     */
    @Transactional
    @RequireUser
    public void deleteProduct(long productId, Long categoryId, PrincipalUser principalUser) {

        User user = blackUserService.checkBlackUser(principalUser);

        DuckuJangter findProduct = duckuJangterRepository.findById(productId)
                .orElseThrow(() -> new DuckwhoException(NOT_FOUND_POST));

        checkItemCategory(categoryId, findProduct);

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

    // 삭제된 글이면 예외
    private void checkDeleteProduct(DuckuJangter findProductDetail) {
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

    // 카테고리 검증
    private ItemCategories checkItemCategory(long categoryId, DuckuJangter findProduct) {
        ItemCategories itemCategories = itemCategoriesRepository.findById(categoryId)
                .orElseThrow(() -> new DuckwhoException(NOT_FOUND_CATEGORY));

        if (findProduct != null && !findProduct.getItemCategories().getId().equals(itemCategories.getId())) {
            throw new DuckwhoException(UNAUTHORIZED_ACCESS);
        }

        log.info("아이템 카테고리 검증완료, 아이템카테고리: {} ", itemCategories.getName());
        return itemCategories;
    }
}
