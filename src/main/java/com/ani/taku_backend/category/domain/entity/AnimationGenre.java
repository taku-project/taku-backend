package com.ani.taku_backend.category.domain.entity;

import java.util.List;

import com.ani.taku_backend.common.baseEntity.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 애니장르 테이블 엔티티
 */
@Table(name = "ani_genres")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class AnimationGenre extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "genre_name" , length = 50)
    private String genreName;

    @OneToMany(mappedBy = "genre")
    private List<CategoryGenre> categoryGenres;

}
