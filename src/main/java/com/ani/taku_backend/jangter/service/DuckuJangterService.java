package com.ani.taku_backend.jangter.service;

import com.ani.taku_backend.common.annotation.CheckViewCount;
import com.ani.taku_backend.common.annotation.RequireUser;
import com.ani.taku_backend.common.annotation.ValidateProfanity;
import com.ani.taku_backend.common.enums.LogType;
import com.ani.taku_backend.common.enums.StatusType;
import com.ani.taku_backend.common.enums.UserRole;
import com.ani.taku_backend.common.enums.ViewType;
import com.ani.taku_backend.common.exception.DuckwhoException;
import com.ani.taku_backend.common.model.entity.Bookmark;
import com.ani.taku_backend.common.model.entity.Image;
import com.ani.taku_backend.common.service.BookmarkService;
import com.ani.taku_backend.common.service.ExtractKeywordService;
import com.ani.taku_backend.common.service.FileService;
import com.ani.taku_backend.common.service.ImageService;
import com.ani.taku_backend.jangter.model.dto.ProductCreateRequestDTO;
import com.ani.taku_backend.jangter.model.dto.ProductFindDetailResponseDTO;
import com.ani.taku_backend.jangter.model.dto.ProductRecommendResponseDTO;
import com.ani.taku_backend.jangter.model.dto.ProductUpdateRequestDTO;
import com.ani.taku_backend.jangter.model.entity.DuckuJangter;
import com.ani.taku_backend.jangter.model.entity.ItemCategories;
import com.ani.taku_backend.jangter.model.entity.JangterImages;
import com.ani.taku_backend.jangter.model.entity.UserInteraction;
import com.ani.taku_backend.jangter.model.entity.UserInteraction.SearchLogDetail;
import com.ani.taku_backend.jangter.model.entity.UserInteraction.ViewLogDetail;
import com.ani.taku_backend.jangter.repository.DuckuJangterRepository;
import com.ani.taku_backend.jangter.repository.ItemCategoriesRepository;
import com.ani.taku_backend.jangter.score.calculator.BookmarkScoreCalculator;
import com.ani.taku_backend.jangter.score.calculator.PurchaseHistoryScoreCalculator;
import com.ani.taku_backend.jangter.score.calculator.SearchHistoryScoreCalculator;
import com.ani.taku_backend.jangter.score.calculator.ViewHistoryScoreCalculator;
import com.ani.taku_backend.jangter.vo.UserBookmarkHistory;
import com.ani.taku_backend.jangter.vo.UserPurchaseHistory;
import com.ani.taku_backend.jangter.vo.UserSearchHistory;
import com.ani.taku_backend.user.model.dto.PrincipalUser;
import com.ani.taku_backend.user.model.entity.User;
import com.ani.taku_backend.user.service.BlackUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.ani.taku_backend.common.exception.ErrorCode.NOT_FOUND_CATEGORY;
import static com.ani.taku_backend.common.exception.ErrorCode.NOT_FOUND_POST;
import static com.ani.taku_backend.common.exception.ErrorCode.UNAUTHORIZED_ACCESS;

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
    private final UserInteractionService userInteractionService;
    private final BookmarkService bookmarkService;
    private final ViewHistoryScoreCalculator viewHistoryScoreCalculator;
    private final SearchHistoryScoreCalculator searchHistoryScoreCalculator;
    private final PurchaseHistoryScoreCalculator purchaseHistoryScoreCalculator;
    private final BookmarkScoreCalculator bookmarkScoreCalculator;
    /**
     * 장터글 저장
     */
    @Transactional
    @ValidateProfanity(fields = {"title", "description"})  // 금칙어 적용 완료
    public Long createProduct(ProductCreateRequestDTO productCreateRequestDTO, User user) {

        ItemCategories itemCategory = checkItemCategory(productCreateRequestDTO.getCategoryId(), null); // 아이템 카테고리 검증

        // 이미지 저장 - r2, rdb 모두 저장
        List<Image> saveImageList = imageService.saveImageList(productCreateRequestDTO.getImageList(), user);
        DuckuJangter product = createProduct(productCreateRequestDTO, user, itemCategory);      // 엔티티 생성
        setRelationJangterImages(saveImageList, product);                                       // jangerImages 연관관계 설정

        Long saveProductId = duckuJangterRepository.save(product).getId();
        log.debug("장터 판매글 등록 완료, 게시글 Id: {}", saveProductId);

        return saveProductId;
    }

    /**
     * 장터글 상세 조회
     */
    @CheckViewCount(viewType = ViewType.SHOP,
            targetId = "#productId",
            expireTime = 60)
    @Transactional
    public ProductFindDetailResponseDTO findProductDetail(long productId, boolean isFirstView) {
        // 판매글 조회
        DuckuJangter findProductDetail = duckuJangterRepository.findById(productId)
                .orElseThrow(() -> new DuckwhoException(NOT_FOUND_POST));

        checkDeleteProduct(findProductDetail);

        // 조회수 증가 로직
        long addViewCount = findProductDetail.addViewCount(isFirstView);

        log.info("장터글 조회 완료, 장터글 상세: {}", findProductDetail);

        return new ProductFindDetailResponseDTO(findProductDetail, findProductDetail.getStatus(), addViewCount);
    }

    /**
     * 게시글 업데이트
     */
    @Transactional
    @ValidateProfanity(fields = {"title", "description"})
    public Long updateProduct(Long productId, ProductUpdateRequestDTO productUpdateRequestDTO, User user) {

        // 게시글 조회
        DuckuJangter findProduct = duckuJangterRepository.findById(productId)
                .orElseThrow(() -> new DuckwhoException(NOT_FOUND_POST));

        checkDeleteProduct(findProduct);    //  삭제 검증
        checkAuthorAndAdmin(user, findProduct);     // 유저, 관리자 확인
        ItemCategories itemCategories = checkItemCategory(productUpdateRequestDTO.getCategoryId(), null);  // 카테고리 검증

        // 업데이트 이미지
        List<Image> newImageList = imageService.getUpdateImageList(productUpdateRequestDTO.getDeleteImageUrl(),
                                                                    productUpdateRequestDTO.getImageList(),
                                                                    user);

        if (newImageList != null && !newImageList.isEmpty()) {
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
    public void deleteProduct(long productId, Long categoryId, User user) {

        DuckuJangter findProduct = duckuJangterRepository.findById(productId)
                .orElseThrow(() -> new DuckwhoException(NOT_FOUND_POST));

        checkItemCategory(categoryId, findProduct);     // 카테고리 검증
        checkAuthorAndAdmin(user, findProduct);         // 유저, 관리자 확인
        checkDeleteProduct(findProduct);                //  삭제 검증

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

    // 아이템 카테고리 검증
    private ItemCategories checkItemCategory(long categoryId, DuckuJangter findProduct) {
        ItemCategories itemCategories = itemCategoriesRepository.findById(categoryId)
                .orElseThrow(() -> new DuckwhoException(NOT_FOUND_CATEGORY));

        if (findProduct != null && !findProduct.getItemCategories().getId().equals(itemCategories.getId())) {
            throw new DuckwhoException(UNAUTHORIZED_ACCESS);
        }

        log.debug("아이템 카테고리 검증완료, 아이템카테고리: {} ", itemCategories.getName());
        return itemCategories;
    }


    /**
     * 장터글 추천
     * @param productId
     * @param principalUser
     * @return
     */
    @RequireUser(allowAnonymous = true)
    @Transactional(readOnly = true)
    public ProductRecommendResponseDTO recommendProduct(Long productId, PrincipalUser principalUser) {

        // 상품상세
        final DuckuJangter product = this.duckuJangterRepository.findById(productId)
                .orElseThrow(() -> new DuckwhoException(NOT_FOUND_POST));

        final Long itemCategoryId = product.getItemCategories().getId();
        

        if(principalUser.isAnonymous()){
            log.info("익명 사용자 추천 상품 조회");
            return getRandomProducts(itemCategoryId, productId);
        }

        
        String title = product.getTitle();
        BigDecimal price = product.getPrice();
        BigDecimal priceRangePercentage = new BigDecimal("0.20"); // 20%
        BigDecimal minPrice = price.subtract(price.multiply(priceRangePercentage));
        BigDecimal maxPrice = price.add(price.multiply(priceRangePercentage));
        log.info("title : {}, price : {}, minPrice : {}, maxPrice : {}", title, price, minPrice, maxPrice);

        List<String> keywords = this.extractKeywordService.extractKeywords(title);
        log.info("keywords : {}", keywords);

        // 1차 필터링 조회
        List<DuckuJangter> recommendProducts = this.duckuJangterRepository
            .findRecommendFilteredProducts(keywords, minPrice, maxPrice, itemCategoryId, StatusType.ACTIVE , productId);

        if(recommendProducts.isEmpty() || recommendProducts.size() < 5){
            log.info("추천 상품 부족으로 랜덤 조회");
            return getRandomProducts(itemCategoryId, productId);
        }
        
        recommendProducts.forEach(item -> {
            log.info("추천 상품: {}", item.getTitle());
        });

        Long userId = principalUser.getUserId();
        // 구매이력 조회
        CompletableFuture<UserPurchaseHistory> purchaseHistoryFuture = CompletableFuture.supplyAsync(() -> getUserPurchaseHistory(userId));
        // 몽고디비 상세조회이력 조회
        CompletableFuture<UserSearchHistory> viewHistoryFuture = CompletableFuture.supplyAsync(() -> getUserViewHistory(userId, productId));
        // 몽고디비 검색이력 조회
        CompletableFuture<UserSearchHistory> searchHistoryFuture = CompletableFuture.supplyAsync(() -> getUserSearchHistory(userId));
        // 찜목록 조회
        CompletableFuture<UserBookmarkHistory> bookmarkHistoryFuture = CompletableFuture.supplyAsync(() -> getUserBookmarkHistory(2l, keywords));

        CompletableFuture.allOf(purchaseHistoryFuture, viewHistoryFuture, searchHistoryFuture, bookmarkHistoryFuture).join();

        // 병렬 처리 후 동기 처리를 위해 대기
        UserPurchaseHistory userPurchaseHistory = purchaseHistoryFuture.join();
        UserSearchHistory userViewHistory = viewHistoryFuture.join();
        log.info("userViewHistory : {}", userViewHistory);
        UserSearchHistory userSearchHistory = searchHistoryFuture.join();
        log.info("userSearchHistory : {}", userSearchHistory);
        UserBookmarkHistory userBookmarkHistory = bookmarkHistoryFuture.join();

        // Thread Safe Map
        Map<Long, Double> productScores = new ConcurrentHashMap<>();

        recommendProducts.parallelStream().forEach(recommendProduct -> {
            // 검색이력 스코어 20%
            double viewHistoryScore = 0.0;
            // 검색이력 스코어 10%
            double searchHistoryScore = 0.0;
            // 구매이력 스코어 50%
            double purchaseHistoryScore = 0.0;
            // 찜목록 스코어 20%
            double bookmarkScore = 0.0;

            // 추천 상품 키워드 추출
            List<String> recommendProductKeywords = this.extractKeywordService.extractKeywords(recommendProduct.getTitle());

            // 각 스코어 계산을 비동기로 실행
            CompletableFuture<Double> viewScoreFuture = CompletableFuture.supplyAsync(() -> 
                this.viewHistoryScoreCalculator.calculate(recommendProduct, recommendProductKeywords, userViewHistory)
            );

            CompletableFuture<Double> searchScoreFuture = CompletableFuture.supplyAsync(() -> 
                this.searchHistoryScoreCalculator.calculate(recommendProduct, recommendProductKeywords, userSearchHistory)
            );

            CompletableFuture<Double> purchaseScoreFuture = CompletableFuture.supplyAsync(() -> 
                this.purchaseHistoryScoreCalculator.calculate(recommendProduct, recommendProductKeywords, userPurchaseHistory)
            );

            CompletableFuture<Double> bookmarkScoreFuture = CompletableFuture.supplyAsync(() -> 
                this.bookmarkScoreCalculator.calculate(recommendProduct, recommendProductKeywords, userBookmarkHistory)
            );

            CompletableFuture.allOf(viewScoreFuture, searchScoreFuture, purchaseScoreFuture, bookmarkScoreFuture).join();

            // 모든 스코어 계산이 완료될 때까지 대기하고 결과 취합
            viewHistoryScore = viewScoreFuture.join();
            searchHistoryScore = searchScoreFuture.join();
            purchaseHistoryScore = purchaseScoreFuture.join();
            bookmarkScore = bookmarkScoreFuture.join();

            // 검색/조회 이력 점수 (30%)
            double searchAndViewHistoryScore = (searchHistoryScore * 0.2 + viewHistoryScore * 0.1) * 0.3;

            // 구매 이력 점수 (50%)
            double purchaseScore = purchaseHistoryScore * 0.5;
            
            // 찜 목록 점수 (20%)
            double bookmarkFinalScore = bookmarkScore * 0.2;

            // 최종 점수 합산 (100%)
            double finalScore = searchAndViewHistoryScore + purchaseScore + bookmarkFinalScore;
            
            // 상품 ID와 최종 점수 저장
            productScores.put(recommendProduct.getId(), finalScore);

            log.info("상품: {}, 최종점수: {}, (조회/검색: {}, 구매: {}, 찜: {})", 
                recommendProduct.getTitle(), 
                finalScore,
                searchAndViewHistoryScore,
                purchaseScore,
                bookmarkFinalScore);
        });

        if(!productScores.isEmpty()) {
            List<DuckuJangter> sortedProducts = recommendProducts.stream()
                // 스코어가 0.0 이상인 상품만 조회
                .filter(item -> productScores.containsKey(item.getId()) && productScores.get(item.getId()) > 0.0)
                .sorted((p1, p2) -> Double.compare(productScores.get(p2.getId()), productScores.get(p1.getId())))
                .limit(5)
                .collect(Collectors.toList());

            // 필터링 후 데이터가 5건 이하일 경우 랜덤 조회
            return sortedProducts.size() >= 5 ? 
                    ProductRecommendResponseDTO.of(sortedProducts) : 
                    getRandomProducts(itemCategoryId, productId);
        }else{
            return ProductRecommendResponseDTO.empty();
        }
    }
    // 어드민이거나, 작성자와 다르면 예외
    private void checkAuthorAndAdmin(User user, DuckuJangter duckuJangter) {
        if ((!user.getRole().equals(UserRole.ADMIN.name())) &&
                !user.getUserId().equals(duckuJangter.getUser().getUserId())) {
            throw new DuckwhoException(UNAUTHORIZED_ACCESS);
        }
    }

    // 삭제된 글이면 예외
    private void checkDeleteProduct(DuckuJangter duckuJangter) {
        if (duckuJangter.getDeletedAt() != null) {
            throw new DuckwhoException(NOT_FOUND_POST);
        }
    }

    private UserPurchaseHistory getUserPurchaseHistory(Long userId) {
        List<DuckuJangter> buyUserProducts = this.duckuJangterRepository.findByBuyUserId(userId);

        if(buyUserProducts.isEmpty()){
            return null;
        }

        List<String> buyUserProductTitleKeywords = buyUserProducts.stream()
            .map(item -> extractKeywordService.extractKeywords(item.getTitle()))
            .flatMap(List::stream)
            .distinct()
            .collect(Collectors.toList());

        return UserPurchaseHistory.create(buyUserProducts, buyUserProductTitleKeywords);
    }


    private UserSearchHistory getUserViewHistory(Long userId , Long productId) {
        List<UserInteraction> viewHistory = this.userInteractionService.findLatestByUserId(userId, LogType.VIEW);
        viewHistory.forEach(item -> {
            log.info("viewHistory : {}", ((ViewLogDetail)item.getLogDetail()).getProductId());
        });
        List<Long> searchedProductIds = viewHistory.stream().map(item -> ((ViewLogDetail)item.getLogDetail()).getProductId()).toList();
        List<DuckuJangter> searchedProducts = this.duckuJangterRepository.findByIdIn(searchedProductIds);

        return UserSearchHistory.create(
            searchedProducts.parallelStream()
                .map(item -> this.extractKeywordService.extractKeywords(item.getTitle()))
                .flatMap(List::stream)
                .distinct()
                .toList(),
            searchedProducts.stream().map(item -> item.getItemCategories().getId()).distinct().toList()
        );
    }

    private UserSearchHistory getUserSearchHistory(Long userId) {
        List<UserInteraction> searchHistory = this.userInteractionService.findLatestByUserId(userId, LogType.SEARCH);
        // 검색이력 키워드 , 카테고리 추출
        UserSearchHistory userSearchHistory = UserSearchHistory.create(
            searchHistory.stream()
                .map(item -> this.extractKeywordService.extractKeywords(((SearchLogDetail)item.getLogDetail()).getSearchKeyword()))
                .flatMap(List::stream)
                .distinct()
                .toList(),
            searchHistory.stream()
                .map(item -> ((SearchLogDetail)item.getLogDetail()).getSearchCategory())
                .distinct()
                .findFirst()
                .orElse(Arrays.asList())
        );

        return userSearchHistory;
    }

    private UserBookmarkHistory getUserBookmarkHistory(Long userId , List<String> keywords) {
        // 사용자 찜목록 조회
        List<Bookmark> bookmarkList = this.bookmarkService.findByUserIdWithJangterAndCategories(userId);
        UserBookmarkHistory userBookmarkHistory = null;
        if(!bookmarkList.isEmpty()){
            List<DuckuJangter> bookmarkedProducts = bookmarkList.stream()
                .flatMap(bookmark -> bookmark.getDuckuJangterBookmarks().stream())
                .map(jangterBookmark -> jangterBookmark.getJangter())
                .toList();

            userBookmarkHistory = UserBookmarkHistory.create(bookmarkedProducts, keywords);
            log.info("################## >>>>>>>>>>> userBookmarkHistory : {}", userBookmarkHistory);
        }

        return userBookmarkHistory;
    }

          private ProductRecommendResponseDTO getRandomProducts(Long categoryId, Long productId) {
        log.info("랜덤 상품 조회");
        List<DuckuJangter> randomProducts = this.duckuJangterRepository.findByCategoryIdRandom(StatusType.ACTIVE.name(), categoryId, productId);
        randomProducts.clear();
        if(randomProducts.size() < 5){
            log.info("전체 카테고리에서 랜덤조회");
            randomProducts.addAll(this.duckuJangterRepository.findRandom(StatusType.ACTIVE.name(), productId));
        }

        return ProductRecommendResponseDTO.of(randomProducts);
    }

}
