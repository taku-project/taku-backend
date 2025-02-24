package com.ani.taku_backend.bookmark.service;

import com.ani.taku_backend.bookmark.domain.Bookmark;

import java.util.List;

public interface BookmarkService {
    List<Bookmark> findByUserIdWithJangterAndCategories(Long userId);

}
