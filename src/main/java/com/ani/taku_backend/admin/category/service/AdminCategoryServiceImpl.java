package com.ani.taku_backend.admin.category.service;

import com.ani.taku_backend.admin.category.domain.CategoryLog;
import com.ani.taku_backend.admin.category.domain.CategoryLogType;
import com.ani.taku_backend.admin.category.domain.dto.req.AdminCategoryListReqDTO;
import com.ani.taku_backend.admin.category.domain.dto.req.UpdateCategoryReqDTO;
import com.ani.taku_backend.admin.category.domain.dto.res.AdminCategoryListResDTO;
import com.ani.taku_backend.admin.category.domain.dto.res.AdminCategoryResDTO;
import com.ani.taku_backend.admin.category.repository.AdminCategoryRepository;
import com.ani.taku_backend.admin.category.repository.CategoryLogRepository;
import com.ani.taku_backend.category.domain.entity.Category;
import com.ani.taku_backend.category.domain.entity.CategoryStatus;
import com.ani.taku_backend.common.exception.DuckwhoException;
import com.ani.taku_backend.common.exception.ErrorCode;
import com.ani.taku_backend.common.exception.UserException;
import com.ani.taku_backend.user.model.entity.User;
import com.ani.taku_backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminCategoryServiceImpl implements AdminCategoryService {
    private final AdminCategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final CategoryLogRepository categoryLogRepository;

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

    @Override
    @Transactional
    public void updateCategoryStatus(User user, UpdateCategoryReqDTO updateCategoryReqDTO) {
        User findUser = userRepository.findById(user.getUserId())
                .orElseThrow(UserException.UserNotFoundException::new);
        Category category = categoryRepository.findById(updateCategoryReqDTO.getCategoryId())
                .orElseThrow(() -> new DuckwhoException(ErrorCode.NOT_FOUND_CATEGORY));

        CategoryStatus updateCategoryStatus = updateCategoryReqDTO.getCategoryStatus();
        category.updateCategoryStatus(updateCategoryStatus);

        String content = category.getName() + "의 상태를" + category.getStatus() + "에서 " + updateCategoryStatus + "로 변경하였습니다.";

        CategoryLog categoryLog = CategoryLog.create(category, user.getUserId(), user.getNickname(), content, CategoryLogType.UPDATE);

        categoryRepository.save(category);
        categoryLogRepository.save(categoryLog);
    }
}
