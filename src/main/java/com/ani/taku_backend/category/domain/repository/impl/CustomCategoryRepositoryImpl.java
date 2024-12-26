package com.ani.taku_backend.category.domain.repository.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeMap;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.ani.taku_backend.category.domain.dto.RequestCategorySearch;
import com.ani.taku_backend.category.domain.dto.ResponseCategorySeachDTO;
import com.ani.taku_backend.category.domain.entity.Category;
import com.ani.taku_backend.category.domain.entity.CategoryGenre;
import com.ani.taku_backend.category.domain.entity.CategoryImage;
import com.ani.taku_backend.category.domain.entity.QAnimationGenre;
import com.ani.taku_backend.category.domain.entity.QCategory;
import com.ani.taku_backend.category.domain.entity.QCategoryGenre;
import com.ani.taku_backend.category.domain.entity.QCategoryImage;
import com.ani.taku_backend.common.model.entity.QImage;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class CustomCategoryRepositoryImpl implements CustomCategoryRepository {

    private final ModelMapper modelMapper;
    private final JPAQueryFactory jpaQueryFactory;

    /**
     * 카테고리 검색 조건과 페이징 정보를 기반으로 카테고리 목록을 조회합니다.
     * 카테고리와 연관된 장르, 이미지 정보를 함께 조회하여 DTO로 변환합니다.
     *
     * @param requestCategorySearch 카테고리 검색 조건 (이름, 장르ID 등)
     * @param pageable 페이징 정보
     * @return 카테고리 검색 결과 DTO 페이지
     */
    @Override
    public Page<ResponseCategorySeachDTO> searchCategories(RequestCategorySearch requestCategorySearch, Pageable pageable) {
        List<Category> fetchCategories = getCategories(requestCategorySearch, pageable);
        List<Long> categoryIds = fetchCategories.stream()
            .map(Category::getId)
            .collect(Collectors.toList());

        // 장르와 이미지 정보를 한번에 조회
        Map<Long, List<CategoryGenre>> genreMap = fetchGenreMap(categoryIds);
        Map<Long, List<CategoryImage>> imageMap = fetchImageMap(categoryIds);

        return new PageImpl<>(
            fetchCategories.stream()
                .map(category -> createResponseDTO(category, genreMap, imageMap))
                .collect(Collectors.toList()),
            pageable,
            getTotalCount(requestCategorySearch)
        );
    }

    /**
     * 검색 조건에 맞는 카테고리 기본 정보를 조회합니다.
     * 카테고리와 연관된 사용자 정보를 함께 조회합니다 (fetch join 사용).
     *
     * @param requestCategorySearch 카테고리 검색 조건
     * @param pageable 페이징 정보
     * @return 조회된 카테고리 목록
     */
    private List<Category> getCategories(RequestCategorySearch requestCategorySearch, Pageable pageable) {
        return jpaQueryFactory
            .selectFrom(QCategory.category)
            .leftJoin(QCategory.category.user).fetchJoin()
            .where(
                nameContains(requestCategorySearch.getName(), QCategory.category),
                genreIdEquals(requestCategorySearch.getGenreIds(), QCategoryGenre.categoryGenre)
            )
            .orderBy(QCategory.category.name.asc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();
    }

    /**
     * 주어진 카테고리 ID 목록에 해당하는 모든 장르 정보를 조회합니다.
     * 카테고리 ID를 키로 하는 Map으로 변환하여 반환합니다.
     *
     * @param categoryIds 조회할 카테고리 ID 목록
     * @return 카테고리 ID를 키로 하고 해당 장르 목록을 값으로 하는 Map
     */
    private Map<Long, List<CategoryGenre>> fetchGenreMap(List<Long> categoryIds) {
        return jpaQueryFactory
            .selectFrom(QCategoryGenre.categoryGenre)
            .join(QCategoryGenre.categoryGenre.genre, QAnimationGenre.animationGenre).fetchJoin()
            .where(QCategoryGenre.categoryGenre.category.id.in(categoryIds))
            .fetch()
            .stream()
            .collect(Collectors.groupingBy(cg -> cg.getCategory().getId()));
    }

    /**
     * 주어진 카테고리 ID 목록에 해당하는 모든 이미지 정보를 조회합니다.
     * 카테고리 ID를 키로 하는 Map으로 변환하여 반환합니다.
     *
     * @param categoryIds 조회할 카테고리 ID 목록
     * @return 카테고리 ID를 키로 하고 해당 이미지 목록을 값으로 하는 Map
     */
    private Map<Long, List<CategoryImage>> fetchImageMap(List<Long> categoryIds) {
        return jpaQueryFactory
            .selectFrom(QCategoryImage.categoryImage)
            .join(QCategoryImage.categoryImage.image, QImage.image).fetchJoin()
            .where(QCategoryImage.categoryImage.category.id.in(categoryIds))
            .fetch()
            .stream()
            .collect(Collectors.groupingBy(ci -> ci.getCategory().getId()));
    }

    /**
     * 카테고리 엔티티와 관련 정보를 DTO로 변환합니다.
     *
     * @param category 변환할 카테고리 엔티티
     * @param genreMap 카테고리별 장르 정보 Map
     * @param imageMap 카테고리별 이미지 정보 Map
     * @return 변환된 카테고리 검색 결과 DTO
     */
    private ResponseCategorySeachDTO createResponseDTO(
            Category category, 
            Map<Long, List<CategoryGenre>> genreMap, 
            Map<Long, List<CategoryImage>> imageMap) {
        
        // 기본 매핑 수행
        ResponseCategorySeachDTO dto = modelMapper.map(category, ResponseCategorySeachDTO.class);
        
        // 디버깅을 위한 로그 추가
        log.info("Category ID: {}", category.getId());
        log.info("Genres Map: {}", genreMap);
        log.info("Images Map: {}", imageMap);
        
        // 장르 정보 매핑
        List<CategoryGenre> genres = genreMap.getOrDefault(category.getId(), Collections.emptyList());
        log.info("Found genres for category {}: {}", category.getId(), genres);
        if (!genres.isEmpty()) {
            modelMapper.map(genres, dto, "genreMapping");
        }
        
        // 이미지 정보 매핑
        List<CategoryImage> images = imageMap.getOrDefault(category.getId(), Collections.emptyList());
        log.info("Found images for category {}: {}", category.getId(), images);
        if (!images.isEmpty()) {
            modelMapper.map(images, dto, "imageMapping");
        }
        
        // 최종 결과 확인
        log.info("Final DTO: {}", dto);
        
        return dto;
    }

    /**
     * 검색 조건에 맞는 전체 카테고리 수를 조회합니다.
     * 페이징 처리를 위한 총 개수를 반환합니다.
     *
     * @param condition 카테고리 검색 조건
     * @return 검색 조건에 맞는 전체 카테고리 수
     */
    private long getTotalCount(RequestCategorySearch condition) {
        return jpaQueryFactory
            .selectFrom(QCategory.category)
            .where(
                nameContains(condition.getName(), QCategory.category),
                genreIdEquals(condition.getGenreIds(), QCategoryGenre.categoryGenre)
            )
            .fetchCount();
    }

    /**
     * 카테고리 이름으로 검색하는 조건을 생성합니다.
     * 검색어가 null인 경우 null을 반환하여 where절에서 무시되도록 합니다.
     *
     * @param name 검색할 카테고리 이름
     * @param category 카테고리 Q타입
     * @return 이름 검색 조건식
     */
    private BooleanExpression nameContains(String name, QCategory category) {
        return name != null ? category.name.containsIgnoreCase(name) : null;
    }

    /**
     * 장르 ID로 검색하는 조건을 생성합니다.
     * 장르 ID가 null인 경우 null을 반환하여 where절에서 무시되도록 합니다.
     *
     * @param genreId 검색할 장르 ID
     * @param categoryGenre 카테고리장르 Q타입
     * @return 장르 ID 검색 조건식
     */
    private BooleanExpression genreIdEquals(List<Long> genreIds, QCategoryGenre categoryGenre) {
        if (genreIds == null || genreIds.isEmpty()) {
            return null;
        }
        
        // 모든 장르를 가지고 있어야 함 (AND 조건)
        return genreIds.stream()
            .map(genreId -> QCategory.category.categoryGenres.any().genre.id.eq(genreId))
            .reduce(BooleanExpression::and)
            .get();
    }
}
