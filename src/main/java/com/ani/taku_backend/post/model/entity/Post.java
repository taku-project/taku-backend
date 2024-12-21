package com.ani.taku_backend.post.model.entity;

import com.ani.taku_backend.category.domain.entity.Category;
import com.ani.taku_backend.common.model.entity.Image;
import com.ani.taku_backend.user.model.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Table(name = "posts")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@ToString
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id") // 외래 키 컬럼 이름 명시
    private Category category;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Image> images = new ArrayList<>();

    private String title;
    private String content;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Long views;
    private Long likes;

    // 조회수 증가
    public void addViews() {
        this.views++;
    }

    // 좋아요 수 증가
    public void addLikes() {
        this.likes++;
    }

    // 좋아요 수 감소
    public void subLikes() {
        if (this.likes > 0) {
            this.likes--;
        }
    }
}
