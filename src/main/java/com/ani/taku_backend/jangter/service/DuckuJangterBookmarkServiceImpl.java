package com.ani.taku_backend.jangter.service;

import com.ani.taku_backend.common.exception.DuckwhoException;
import com.ani.taku_backend.common.exception.ErrorCode;
import com.ani.taku_backend.jangter.model.dto.BookmarkListResponseDTO;
import com.ani.taku_backend.jangter.model.entity.DuckuJangter;
import com.ani.taku_backend.jangter.model.entity.DuckuJangterBookmark;
import com.ani.taku_backend.jangter.repository.DuckuJangterBookmarkRepository;
import com.ani.taku_backend.jangter.repository.DuckuJangterRepository;
import com.ani.taku_backend.category.domain.repository.CategoryRepository;
import com.ani.taku_backend.jangter.vo.UserBookmarkHistory;
import com.ani.taku_backend.user.model.entity.User;
import com.ani.taku_backend.user.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DuckuJangterBookmarkServiceImpl implements DuckuJangterBookmarkService {

    private final DuckuJangterBookmarkRepository bookmarkRepository;
    private final DuckuJangterRepository jangterRepository;
    private final CategoryRepository CategoryRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void addBookmark(Long userId, Long productId) {
        // 활성 상태와 관계없이 북마크 존재여부 확인
        Optional<DuckuJangterBookmark> existingBookmark = bookmarkRepository.findByUserUserIdAndJangterId(userId, productId);
        
        if (existingBookmark.isPresent()) {
            DuckuJangterBookmark bookmark = existingBookmark.get();
            // 북마크가 이미 활성 상태인 경우
            if (bookmark.getIsActive()) {
                throw new DuckwhoException(ErrorCode.ALREADY_BOOKMARKED);
            }
            // 북마크가 비활성 상태인 경우 재활성화
            bookmark.activate();
            return;
        }

        // 북마크가 없는 경우 새로 생성
        DuckuJangter jangter = jangterRepository.findById(productId)
                .orElseThrow(() -> new DuckwhoException(ErrorCode.PRODUCT_NOT_FOUND));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new DuckwhoException(ErrorCode.USER_NOT_FOUND));
                
        bookmarkRepository.save(DuckuJangterBookmark.create(user, jangter));
    }

    @Override
    @Transactional
    public void removeBookmark(Long userId, Long productId) {
        DuckuJangterBookmark bookmark = bookmarkRepository.findByUserUserIdAndJangterId(userId, productId)
                .orElseThrow(() -> new DuckwhoException(ErrorCode.NOT_FOUND_BOOKMARK));
        
        bookmark.deactivate();
    }

    @Override
    public Page<BookmarkListResponseDTO> getBookmarkList(Long userId, Long categoryId, Pageable pageable) {
        Long filteredCategoryId = categoryId;

        if (categoryId != 0) {
            // categoryId가 0이 아니면 존재 여부 확인
            if (!CategoryRepository.existsById(categoryId)) {
                throw new DuckwhoException(ErrorCode.NOT_FOUND_CATEGORY);
            }
            // 카테고리가 존재하면 filteredCategoryId는 그대로 유지
        } else {
            // categoryId가 0이면 모든 카테고리 조회 (null 설정)
            filteredCategoryId = null;
        }

        return bookmarkRepository.findBookmarksByUserIdWithPaging(userId, filteredCategoryId, pageable);
    }

    @Override
    public UserBookmarkHistory getUserBookmarkHistory(Long userId, List<String> keywords) {
        List<DuckuJangterBookmark> userBookmarks = bookmarkRepository.findByUserUserIdAndIsActiveTrue(userId);

        if (userBookmarks.isEmpty()) {
            return null;
        }

        List<DuckuJangter> bookmarkedProducts = userBookmarks.stream()
                .map(DuckuJangterBookmark::getJangter)
                .toList();

        return UserBookmarkHistory.create(bookmarkedProducts, keywords);
    }
}