package com.ani.taku_backend.category.domain.entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.ani.taku_backend.category.domain.dto.RequestCategoryCreateDTO;
import com.ani.taku_backend.common.baseEntity.BaseTimeEntity;
import com.ani.taku_backend.common.enums.StatusType;
import com.ani.taku_backend.user.model.entity.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 
 * 카테고리 테이블 엔티티
 */
@Builder
@Table(name = "categories")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Category extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "name" , length = 255)
    private String name;

    @Column(name = "created_type" , length = 255)
    private String createdType;

    @Column(name = "status" , length = 100)
    private String status;

    @Column(name = "view_count")
    private Long viewCount;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL)
    private List<CategoryImage> categoryImages;

    // TODO : 카테고리 애니장르
    @OneToMany(mappedBy = "category" , cascade = CascadeType.ALL)
    private List<CategoryGenre> categoryGenres;

    // 카테고리 첫 생성
    public static Category from(RequestCategoryCreateDTO requestCategoryCreateDTO, User user){
        return Category.builder()
            .name(requestCategoryCreateDTO.getName())
            .createdType(user.getRole())
            .viewCount(0L)
            .status(StatusType.INACTIVE.name())
            .user(user)
            .categoryImages(new ArrayList<>()) 
            .categoryGenres(new ArrayList<>())
            .build();
    }



}
