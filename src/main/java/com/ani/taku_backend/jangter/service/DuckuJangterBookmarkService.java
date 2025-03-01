package com.ani.taku_backend.jangter.service;

import com.ani.taku_backend.jangter.model.dto.BookmarkListResponseDTO;
import com.ani.taku_backend.jangter.model.entity.DuckuJangterBookmark;
import com.ani.taku_backend.jangter.vo.UserBookmarkHistory;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DuckuJangterBookmarkService {

    /**
     * 장터 상품을 북마크에 추가합니다.
     * @param userId 사용자 ID
     * @param productId 상품 ID
     */
    void addBookmark(Long userId, Long productId);

    /**
     * 장터 상품을 북마크에서 제거합니다.
     * @param userId 사용자 ID
     * @param productId 상품 ID
     */
    void removeBookmark(Long userId, Long productId);

    /**
     * 사용자의 장터 북마크 목록을 조회합니다.
     * @param userId 사용자 ID
     * @param categoryId 카테고리 ID (0: 전체 조회)
     * @param pageable 페이징 정보
     * @return 북마크 목록
     */
    Page<BookmarkListResponseDTO> getBookmarkList(Long userId, Long categoryId, Pageable pageable);


    /**
     * 사용자의 북마크 이력을 키워드와 함께 분석하여 추천 시스템에 활용할 수 있는 데이터를 제공합니다.
     * @param userId 사용자 ID
     * @param keywords 분석에 사용할 키워드 목록
     * @return 사용자의 북마크 이력 정보를 담은 객체, 북마크가 없는 경우 null 반환
     */
    UserBookmarkHistory getUserBookmarkHistory(Long userId, List<String> keywords);
}
