package com.ani.taku_backend.jangter.service;

import com.ani.taku_backend.common.annotation.RequireUser;
import com.ani.taku_backend.common.annotation.ValidateProfanity;
import com.ani.taku_backend.common.enums.StatusType;
import com.ani.taku_backend.common.exception.DuckwhoException;
import com.ani.taku_backend.common.model.entity.Image;
import com.ani.taku_backend.common.repository.ImageRepository;
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
import com.ani.taku_backend.user.model.dto.PrincipalUser;
import com.ani.taku_backend.user.model.entity.BlackUser;
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
    public ProductFindDetailResponseDTO findProductDetail(Long productId) {

        // 판매글 조회
        DuckuJangter findProductDetail = duckuJangterRepository.findById(productId)
                .orElseThrow(() -> new DuckwhoException(NOT_FOUND_POST));

        validateDelete(findProductDetail);

        // 조회 수 증가 -> 나중에 중복 방지 AOP 적용, (진호님 개발된 AOP 적용 해야함)
        long viewCount = findProductDetail.addViewCount();
        log.info("장터글 조회 완료, 장터글 상세: {}", findProductDetail);

        return new ProductFindDetailResponseDTO(findProductDetail, findProductDetail.getStatus(), viewCount);
    }

    private void validateDelete(DuckuJangter findProductDetail) {
        // 삭제된 글이면 예외
        if (findProductDetail.getDeletedAt() != null) {
            throw new DuckwhoException(NOT_FOUND_POST);
        }
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
        findProduct.updateTitle(productUpdateRequestDTO.getTitle());
        findProduct.updateDescription(productUpdateRequestDTO.getDescription());
        findProduct.updatePrice(productUpdateRequestDTO.getPrice());
        findProduct.updateItemCategory(itemCategories);

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
            fileService.deleteFile(jangterImages.getImage().getFileName());
        });
        log.info("장터글 삭제 완료 - 삭제일: {}", findProduct.getDeletedAt());
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

        if (findProduct != null && !findProduct.getItemCategory().getId().equals(itemCategories.getId())) {
            throw new DuckwhoException(UNAUTHORIZED_ACCESS);
        }

        return itemCategories;
    }
}
