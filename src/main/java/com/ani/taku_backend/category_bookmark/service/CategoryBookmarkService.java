package com.ani.taku_backend.category_bookmark.service;

import com.ani.taku_backend.category_bookmark.dto.res.CategoryBookmarkReqDTO;
import com.ani.taku_backend.user.model.entity.User;

public interface CategoryBookmarkService {
    void createCategoryBookmark(User user, Long categoryId);

    void deleteCategoryBookmark(User user, Long categoryId);

    CategoryBookmarkReqDTO findCategoryBookmark(User user);
}
