package com.ani.taku_backend.jangter.service;

import com.ani.taku_backend.common.annotation.RequireUser;
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

    // 판매글 엔티티 생성
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

    // 아이템 카테고리 카테고리 검증

    /**
     * 게시글 상세 조회
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

        // 유저 조회 검증 -> 블랙유저 검증 필요
        User user = principalUser.getUser();
        List<BlackUser> byUserId = blackUserService.findByUserId(user.getUserId());

        if (!user.getNickname().equals(findProduct.getUser().getNickname())) {  // 본인 글인지 확인
            throw new DuckwhoException(UNAUTHORIZED_ACCESS);
        }

        // 마찬가지로.. 금칙어 적용

        // 카테고리 가져오기
        ItemCategories itemCategories = getItemCategories(productUpdateRequestDTO.getCategoryId());

        // 게시글에서 첨부파일을 모두 삭제하고 넘어옴
        if (imageList == null || imageList.isEmpty()) {
            findProduct.getJangterImages().forEach(communityImage -> {
                Image image = communityImage.getImage();
                fileService.deleteFile(image.getFileName());    // s3 에서 삭제(클라우드 플레어)
                image.softDelete();                             // RDB에서 삭제 일시 입력
            });
        }

        // db에서 상품id로 이미지 조회
        List<Image> findImageList = imageRepository.findImageByproductId(productId);
        List<String> requestImageUrlList = productUpdateRequestDTO.getImageUrl();   // 기존에 등록된 이미지

        // 삭제 대상인 이미지 리스트 -> db에서 조회한 이미지와 넘어온 이미지가 다르면 db에서 조회한 이미지는 삭제대상
        List<Image> deleteImageList = findImageList.stream()
                .filter(image -> !requestImageUrlList.contains(image.getImageUrl())).toList();
                        deleteImageList.forEach(image -> {
                            fileService.deleteFile(image.getFileName());    // r2에서 파일 삭제
                            image.softDelete();                             // RDB soft delete
                        }
                );

        // db에서 조회한 이미지리스트와 요청으로 넘어온 imageList의 파일사이즈와 오리지널파일네임이 같지 않으면 imageList들은 새로 저장할 이미지라고 가정
        List<MultipartFile> newImageList = imageList.stream()
                .filter(multipartFile -> findImageList.stream()
                        .noneMatch(image ->
                                image.getFileSize() == multipartFile.getSize() &&
                                image.getOriginalName().equals(multipartFile.getOriginalFilename())
                        )
                ).toList();

        if (!newImageList.isEmpty()) {
            List<Image> saveImageList = imageService.saveImageList(newImageList, user); // 이미지 저장
            setRelationJangterImages(saveImageList, findProduct);   // 연관관계 설정
        }

        findProduct.updateDuckuJangter(productUpdateRequestDTO.getTitle(),
                                        productUpdateRequestDTO.getDescription(),
                                        productUpdateRequestDTO.getPrice(), itemCategories);

        return findProduct.getId();
    }


    private ItemCategories getItemCategories(Long categoryId) {
        return itemCategoriesRepository.findById(categoryId)
                .orElseThrow(() -> new DuckwhoException(NOT_FOUND_CATEGORY));
    }


}
