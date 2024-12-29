package com.ani.taku_backend.jangter.service;

import com.ani.taku_backend.common.annotation.RequireUser;
import com.ani.taku_backend.common.enums.StatusType;
import com.ani.taku_backend.common.exception.DuckwhoException;
import com.ani.taku_backend.common.model.entity.Image;
import com.ani.taku_backend.common.service.ImageService;
import com.ani.taku_backend.jangter.model.dto.ProductCreateRequestDTO;
import com.ani.taku_backend.jangter.model.dto.ProductFindDetailResponseDTO;
import com.ani.taku_backend.jangter.model.entity.DuckuJangter;
import com.ani.taku_backend.jangter.model.entity.ItemCategories;
import com.ani.taku_backend.jangter.model.entity.JangterImages;
import com.ani.taku_backend.jangter.repository.DuckuJangterRepository;
import com.ani.taku_backend.jangter.repository.ItemCategoriesRepository;
import com.ani.taku_backend.jangter.service.viewcount.ViewCountService;
import com.ani.taku_backend.user.model.dto.PrincipalUser;
import com.ani.taku_backend.user.model.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static com.ani.taku_backend.common.exception.ErrorCode.NOT_FOUND_CATEGORY;
import static com.ani.taku_backend.common.exception.ErrorCode.NOT_FOUND_POST;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DuckuJangterService {

    private final DuckuJangterRepository duckuJangterRepository;
    private final ItemCategoriesRepository itemCategoriesRepository;
    private final ViewCountService viewCountService;
    private final ImageService imageService;

    @Transactional
    @RequireUser
    public Long createProduct(ProductCreateRequestDTO productCreateRequestDTO,
                              List<MultipartFile> imageList,
                              PrincipalUser principalUser) {
        // 유저 조회 -> 검증 로직 필요, 본인인가?, 관리자도 접속가능, 블랙유저인가,
        User user = principalUser.getUser();

        // 금칙어 aop 적용해야함 -> 댓글 작업까지 하고..

        ItemCategories findItemCategory = getItemCategories(productCreateRequestDTO);
        DuckuJangter product = createProduct(productCreateRequestDTO, user, findItemCategory);

        List<String> imageUrlList= imageService.uploadProductImageList(imageList);              // 클라우드플레어 저장
        List<Image> saveImageList = imageService.saveImageList(imageList, user, imageUrlList);  // mysql 저장

        // jangerImages 연관관계 설정
        setRelation(saveImageList, product);

        Long saveProductId = duckuJangterRepository.save(product).getId();
        log.info("장터 판매글 등록 완료, 게시글 Id: {}",saveProductId);

        return saveProductId;
    }

    private void setRelation(List<Image> saveImageList, DuckuJangter product) {
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

    // 아티템 카테고리 카테고리 검증
    private ItemCategories getItemCategories(ProductCreateRequestDTO productCreateRequestDTO) {
        return itemCategoriesRepository.findById(productCreateRequestDTO.getCategoryId())
                .orElseThrow(() -> new DuckwhoException(NOT_FOUND_CATEGORY));
    }

    @Transactional
    public ProductFindDetailResponseDTO findProductDetail(Long productId) {

        DuckuJangter findProductDetail = duckuJangterRepository.findById(productId)
                .orElseThrow(() -> new DuckwhoException(NOT_FOUND_POST));

        // 조회 수 증가 및 조회수 가져오기
        viewCountService.incrementViewCount(productId);
        Long viewCount = viewCountService.getViewCount(productId);

        return new ProductFindDetailResponseDTO(findProductDetail, findProductDetail.getStatus(), viewCount);
    }

}
