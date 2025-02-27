package com.ani.taku_backend.jangter.service;

import com.ani.taku_backend.common.exception.DuckwhoException;
import com.ani.taku_backend.common.exception.ErrorCode;
import com.ani.taku_backend.jangter.model.dto.BookmarkListResponseDTO;
import com.ani.taku_backend.jangter.model.entity.DuckuJangter;
import com.ani.taku_backend.jangter.model.entity.DuckuJangterBookmark;
import com.ani.taku_backend.jangter.repository.DuckuJangterBookmarkRepository;
import com.ani.taku_backend.jangter.repository.DuckuJangterRepository;
import com.ani.taku_backend.category.domain.repository.CategoryRepository;
import com.ani.taku_backend.user.model.entity.User;
import com.ani.taku_backend.user.repository.UserRepository;
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
        if (bookmarkRepository.existsByUserUserIdAndJangterId(userId, productId)) {
            throw new DuckwhoException(ErrorCode.ALREADY_BOOKMARKED);
        }

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
        Long effectiveCategoryId = categoryId;
        
        if (categoryId != 0) {
            CategoryRepository.findById(categoryId)
                    .orElseThrow(() -> new DuckwhoException(ErrorCode.NOT_FOUND_CATEGORY));
        } else {
            effectiveCategoryId = null;
        }
        
        return bookmarkRepository.findBookmarksByUserIdWithPaging(userId, effectiveCategoryId, pageable);
    }
}