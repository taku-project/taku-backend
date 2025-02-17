package com.ani.taku_backend.bookmark.service;

import com.ani.taku_backend.bookmark.domain.Bookmark;
import com.ani.taku_backend.bookmark.domain.dto.DuckuJangterBookmarkResponseDTO;
import com.ani.taku_backend.bookmark.domain.repository.BookmarkRepository;
import com.ani.taku_backend.bookmark.domain.repository.DuckuJangterBookmarkRepository;
import com.ani.taku_backend.common.exception.DuckwhoException;
import com.ani.taku_backend.common.exception.ErrorCode;
import com.ani.taku_backend.jangter.model.entity.DuckuJangter;
import com.ani.taku_backend.jangter.model.entity.ItemCategories;
import com.ani.taku_backend.jangter.repository.DuckuJangterRepository;
import com.ani.taku_backend.jangter.repository.ItemCategoriesRepository;
import com.ani.taku_backend.user.model.entity.User;
import com.ani.taku_backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DuckuJangterBookmarkServiceImpl implements DuckuJangterBookmarkService {

    private final BookmarkRepository bookmarkRepository;
    private final DuckuJangterBookmarkRepository duckuJangterBookmarkRepository;
    private final UserRepository userRepository;
    private final DuckuJangterRepository duckuJangterRepository;
    private final ItemCategoriesRepository itemCategoriesRepository;

    @Override
    @Transactional
    public void addBookmark(Long userId, Long productId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new DuckwhoException(ErrorCode.NOT_FOUND_USER));
        
        DuckuJangter product = duckuJangterRepository.findById(productId)
                .orElseThrow(() -> new DuckwhoException(ErrorCode.NOT_FOUND_POST));

        Bookmark bookmark = bookmarkRepository.findByUserId(userId)
                .stream()
                .filter(b -> b.getIsActive())
                .findFirst()
                .orElseGet(() -> bookmarkRepository.save(Bookmark.builder()
                        .user(user)
                        .isActive(true)
                        .build()));

        if (duckuJangterBookmarkRepository.findByBookmarkAndJangter(bookmark, product).isPresent()) {
            throw new DuckwhoException(ErrorCode.ALREADY_BOOKMARKED);
        }

        duckuJangterBookmarkRepository.save(com.ani.taku_backend.jangter.model.entity.DuckuJangterBookmark.builder()
                .bookmark(bookmark)
                .jangter(product)
                .build());
    }

    @Override
    @Transactional
    public void removeBookmark(Long userId, Long productId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new DuckwhoException(ErrorCode.NOT_FOUND_USER));
        
        DuckuJangter product = duckuJangterRepository.findById(productId)
                .orElseThrow(() -> new DuckwhoException(ErrorCode.NOT_FOUND_POST));

        Bookmark bookmark = bookmarkRepository.findByUserId(userId)
                .stream()
                .filter(b -> b.getIsActive())
                .findFirst()
                .orElseThrow(() -> new DuckwhoException(ErrorCode.NOT_FOUND_BOOKMARK));

        duckuJangterBookmarkRepository.deleteByBookmarkAndJangter(bookmark, product);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DuckuJangterBookmarkResponseDTO> getBookmarks(Long userId, Pageable pageable) {
        Bookmark bookmark = bookmarkRepository.findByUserId(userId)
                .stream()
                .filter(b -> b.getIsActive())
                .findFirst()
                .orElseThrow(() -> new DuckwhoException(ErrorCode.NOT_FOUND_BOOKMARK));

        return duckuJangterBookmarkRepository.findAllByBookmarkWithJangter(bookmark, pageable)
                .map(DuckuJangterBookmarkResponseDTO::from);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DuckuJangterBookmarkResponseDTO> getBookmarksByCategory(Long userId, String category, Pageable pageable) {
        Bookmark bookmark = bookmarkRepository.findByUserId(userId)
                .stream()
                .filter(b -> b.getIsActive())
                .findFirst()
                .orElseThrow(() -> new DuckwhoException(ErrorCode.NOT_FOUND_BOOKMARK));

        // 카테고리가 존재하는지 검증
        List<ItemCategories> itemCategories = itemCategoriesRepository.findAll();
        boolean categoryExists = itemCategories.stream()
                .anyMatch(ic -> ic.getName().equals(category));
        
        if (!categoryExists) {
            throw new DuckwhoException(ErrorCode.NOT_FOUND_CATEGORY);
        }

        return duckuJangterBookmarkRepository.findAllByBookmarkAndCategoryWithJangter(bookmark, category, pageable)
                .map(DuckuJangterBookmarkResponseDTO::from);
    }
} 
