package com.ani.taku_backend.post.model.entity;

import com.ani.taku_backend.category.domain.entity.Category;
import com.ani.taku_backend.user.model.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 커뮤니티 게시글 Entity
 */
@Table(name = "posts")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@ToString
@Builder
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

    @OneToMany(mappedBy = "post")
    private List<CommunityImage> communityImages;

    private String title;
    private String content;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Long views;
    private Long likes;

    private LocalDateTime deletedAt ;

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
