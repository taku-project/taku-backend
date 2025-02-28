package com.ani.taku_backend.jangter.service;

import com.ani.taku_backend.bookmark.domain.Bookmark;
import com.ani.taku_backend.bookmark.service.BookmarkService;
import com.ani.taku_backend.common.exception.DuckwhoException;
import com.ani.taku_backend.common.exception.ErrorCode;
import com.ani.taku_backend.jangter.model.dto.BookmarkListResponseDTO;
import com.ani.taku_backend.jangter.model.entity.DuckuJangter;
import com.ani.taku_backend.jangter.model.entity.DuckuJangterBookmark;
import com.ani.taku_backend.jangter.repository.DuckuJangterBookmarkRepository;
import com.ani.taku_backend.jangter.repository.DuckuJangterRepository;
import com.ani.taku_backend.category.domain.repository.CategoryRepository;
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
    private final BookmarkService bookmarkService;
    private final CategoryRepository CategoryRepository;

    @Override
    @Transactional
    public void addBookmark(Long userId, Long productId) {
        if (bookmarkRepository.existsByBookmark_User_UserIdAndJangter_Id(userId, productId)) {
            throw new DuckwhoException(ErrorCode.ALREADY_BOOKMARKED);
        }

        DuckuJangter jangter = jangterRepository.findById(productId)
                .orElseThrow(() -> new DuckwhoException(ErrorCode.PRODUCT_NOT_FOUND));

        Bookmark userBookmark = bookmarkService.getBookmarkByUserId(userId);

        bookmarkRepository.save(DuckuJangterBookmark.builder()
                .bookmark(userBookmark)
                .jangter(jangter)
                .build());
    }

    @Override
    @Transactional
    public void removeBookmark(Long userId, Long productId) {
        if (!bookmarkRepository.existsByBookmark_User_UserIdAndJangter_Id(userId, productId)) {
            throw new DuckwhoException(ErrorCode.NOT_FOUND_BOOKMARK);
        }

        bookmarkRepository.deleteByBookmark_User_UserIdAndJangter_Id(userId, productId);
    }

    @Override
    public Page<BookmarkListResponseDTO> getBookmarkList(Long userId, Long categoryId, Pageable pageable) {
        Long effectiveCategoryId = categoryId;
        
        // 전체 조회가 아닌 경우, 카테고리 존재 여부 확인
        if (categoryId != 0) {
            CategoryRepository.findById(categoryId)
                    .orElseThrow(() -> new DuckwhoException(ErrorCode.NOT_FOUND_CATEGORY));
        } else {
            effectiveCategoryId = null;
        }
        
        return bookmarkRepository.findBookmarksByUserIdWithPaging(userId, effectiveCategoryId, pageable);
    }
}