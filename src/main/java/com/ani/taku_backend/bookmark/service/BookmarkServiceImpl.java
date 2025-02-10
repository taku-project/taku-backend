package com.ani.taku_backend.bookmark.service;

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

    public List<Bookmark> findByUserId(Long userId) {
        return bookmarkRepository.findByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Bookmark> findByUserIdWithJangterAndCategories(Long userId) {
        return bookmarkRepository.findByUserIdWithJangterAndCategories(userId);  // Fetch join 사용
    }

    
    
}

