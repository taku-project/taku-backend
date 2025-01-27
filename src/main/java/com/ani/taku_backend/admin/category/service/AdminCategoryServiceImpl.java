package com.ani.taku_backend.admin.category.service;

import com.ani.taku_backend.admin.category.dto.req.AdminCategoryListReqDTO;
import com.ani.taku_backend.admin.category.dto.res.AdminCategoryListResDTO;
import com.ani.taku_backend.admin.category.dto.res.AdminCategoryResDTO;
import com.ani.taku_backend.admin.category.repository.AdminCategoryRepository;
import com.ani.taku_backend.category.domain.entity.Category;
import com.ani.taku_backend.common.exception.UserException;
import com.ani.taku_backend.user.model.entity.User;
import com.ani.taku_backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminCategoryServiceImpl implements AdminCategoryService {
    private final AdminCategoryRepository categoryRepository;
    private final UserRepository userRepository;

    @Override
    public AdminCategoryListResDTO findCategoryList(User user, AdminCategoryListReqDTO categoryListReqDTO) {
        User findUser = userRepository.findById(user.getUserId())
                .orElseThrow(UserException.UserNotFoundException::new);
        Page<Category> categoryList = categoryRepository.findCategoryList(user.getUserId(), categoryListReqDTO);
        List<AdminCategoryResDTO> adminCategoryResDTOList = categoryList.map(AdminCategoryResDTO::new).toList();

        return AdminCategoryListResDTO.builder()
                .categoryList(
                    new PageImpl<>(
                        adminCategoryResDTOList, categoryList.getPageable(), categoryList.getTotalElements()
                    )
                )
                .build();
    }
}
