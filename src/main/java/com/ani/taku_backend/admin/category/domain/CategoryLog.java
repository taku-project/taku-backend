package com.ani.taku_backend.admin.category.domain;

import com.ani.taku_backend.category.domain.entity.Category;
import com.ani.taku_backend.common.baseEntity.BaseTimeEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "category_log")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class CategoryLog extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private Long categoryId;
    private Long userId;
    private String userName;
    private String content;

    @Enumerated
    private CategoryLogType type;


    public static CategoryLog create(Category category, Long userId, String userName, String content, CategoryLogType type) {
        return CategoryLog.builder()
                .categoryId(category.getId())
                .userId(userId)
                .userName(userName)
                .content(content)
                .type(type)
                .build();
    }
}
