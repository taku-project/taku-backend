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
        bookmarkRepository.findByBookmark_User_UserIdAndJangter_Id(userId, productId)
                .ifPresentOrElse(
                        bookmarkRepository::delete,
                        () -> {
                            throw new DuckwhoException(ErrorCode.NOT_FOUND_CATEGORY_BOOKMARK);
                        }
                );
    }

    @Override
    public Page<BookmarkListResponseDTO> getBookmarkList(Long userId, Long categoryId, Pageable pageable) {
        // categoryId가 0인 경우 전체 조회
        Long effectiveCategoryId = categoryId == 0 ? null : categoryId;
        return bookmarkRepository.findBookmarksByUserIdWithPaging(userId, effectiveCategoryId, pageable);
    }
}