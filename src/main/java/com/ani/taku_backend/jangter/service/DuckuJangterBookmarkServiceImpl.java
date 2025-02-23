package com.ani.taku_backend.jangter.service;

import com.ani.taku_backend.jangter.model.dto.BookmarkListResponseDTO;
import com.ani.taku_backend.jangter.model.entity.DuckuJangter;
import com.ani.taku_backend.jangter.model.entity.DuckuJangterBookmark;
import com.ani.taku_backend.jangter.repository.DuckuJangterBookmarkRepository;
import com.ani.taku_backend.jangter.repository.DuckuJangterRepository;
import com.ani.taku_backend.bookmark.domain.Bookmark;
import com.ani.taku_backend.bookmark.service.BookmarkService;
import com.ani.taku_backend.common.exception.DuckwhoException;
import com.ani.taku_backend.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DuckuJangterBookmarkServiceImpl implements DuckuJangterBookmarkService {

    private final DuckuJangterBookmarkRepository bookmarkRepository;
    private final DuckuJangterRepository jangterRepository;
    private final BookmarkService bookmarkService;

    @Override
    public void addBookmark(Long userId, Long jangterId) {
        if (bookmarkRepository.existsByBookmark_User_UserIdAndJangter_Id(userId, jangterId)) {
            throw new DuckwhoException(ErrorCode.ALREADY_BOOKMARKED);
        }

        DuckuJangter jangter = jangterRepository.findById(jangterId)
                .orElseThrow(() -> new DuckwhoException(ErrorCode. PRODUCT_NOT_FOUND));

        Bookmark userBookmark = bookmarkService.getBookmarkByUserId(userId);

        DuckuJangterBookmark bookmarkEntity = DuckuJangterBookmark.builder()
                .bookmark(userBookmark)
                .jangter(jangter)
                .build();

        bookmarkRepository.save(bookmarkEntity);
    }

    @Override
    public void removeBookmark(Long userId, Long jangterId) {
        DuckuJangterBookmark bookmarkEntity = bookmarkRepository
                .findByBookmark_User_UserIdAndJangter_Id(userId, jangterId)
                .orElseThrow(() -> new DuckwhoException(ErrorCode.NOT_FOUND_CATEGORY_BOOKMARK));
        bookmarkRepository.delete(bookmarkEntity);
    }

    @Override
    public Page<BookmarkListResponseDTO> getBookmarkList(Long userId, Long categoryId, Pageable pageable) {
        return bookmarkRepository.findBookmarksByUserIdWithPaging(userId, categoryId, pageable);
    }

    @Override
    public java.util.List<BookmarkListResponseDTO> getBookmarkListByCategory(Long userId, Long categoryId) {
        return bookmarkRepository.findBookmarksByUserIdAndCategoryId(userId, categoryId);
    }
}