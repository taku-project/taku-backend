package com.ani.taku_backend.bookmark.service;

import com.ani.taku_backend.common.exception.DuckwhoException;
import com.ani.taku_backend.common.exception.ErrorCode;
import com.ani.taku_backend.user.model.entity.User;
import com.ani.taku_backend.user.repository.UserRepository;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ani.taku_backend.bookmark.domain.Bookmark;
import com.ani.taku_backend.bookmark.domain.repository.BookmarkRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BookmarkServiceImpl implements BookmarkService {

    private final BookmarkRepository bookmarkRepository;
    private final UserRepository userRepository;

    public List<Bookmark> findByUserId(Long userId) {
        return bookmarkRepository.findByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Bookmark> findByUserIdWithJangterAndCategories(Long userId) {
        return bookmarkRepository.findByUserIdWithJangterAndCategories(userId);  // Fetch join 사용
    }

    @Override
    @Transactional
    public Bookmark getBookmarkByUserId(Long userId) {
        return bookmarkRepository.findByUser_UserId(userId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new DuckwhoException(ErrorCode.USER_NOT_FOUND));

                    Bookmark newBookmark = Bookmark.builder()
                            .user(user)
                            .build();

                    return bookmarkRepository.save(newBookmark);
                });
    }

    
    
}

