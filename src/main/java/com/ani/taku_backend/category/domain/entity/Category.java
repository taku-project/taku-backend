package com.ani.taku_backend.category.domain.entity;

import com.ani.taku_backend.common.baseEntity.BaseTimeEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "categories")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Category extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private Long id;
    private String name;

    // Animation 장르
    @OneToOne(fetch = FetchType.LAZY)
    private AnimationGenre genre;
    // TODO USER 추가
    // TODO User 타입 추가
    private String createdType;
}
