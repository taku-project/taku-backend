package com.ani.taku_backend.common.model.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import com.ani.taku_backend.user.model.entity.User;

import java.util.List;

import com.ani.taku_backend.common.baseEntity.BaseTimeEntity;
import com.ani.taku_backend.jangter.model.entity.DuckuJangterBookmark;

@Entity
@Table(name = "bookmarks")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@ToString(exclude = {"duckuJangterBookmarks"})
public class Bookmark extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "is_active")
    private Boolean isActive;

    // 양방향 관계 설정
    @OneToMany(mappedBy = "bookmark", cascade = CascadeType.ALL , fetch = FetchType.LAZY)
    private List<DuckuJangterBookmark> duckuJangterBookmarks;

    // TODO: 카테고리 북마크 추가
}
