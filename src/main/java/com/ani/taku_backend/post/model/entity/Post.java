package com.ani.taku_backend.post.model.entity;

import com.ani.taku_backend.category.domain.entity.Category;
import com.ani.taku_backend.common.baseEntity.BaseTimeEntity;
import com.ani.taku_backend.user.model.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
public class Post extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id") // 외래 키 컬럼 이름 명시
    private Category category;

    @Builder.Default
    @BatchSize(size = 1000)
    @OneToMany(mappedBy = "post", cascade = CascadeType.PERSIST)
    private List<CommunityImage> communityImages = new ArrayList<>();

    private String title;
    private String content;

    private Long views;
    private Long likes;

    private LocalDateTime deletedAt ;

    /**
     * 이미지 연관관계 편의 메서드
     */
    public void addCommunityImage(CommunityImage communityImage) {
        this.communityImages.add(communityImage);
        communityImage.assignPost(this);
    }

    public void removeCommunityImage(CommunityImage communityImage) {
        this.communityImages.remove(communityImage);
        communityImage.unassignPost();
    }

    /**
     * User 연관관계 편의 메서드
     */
    public void setUserInternal(User user) {
        this.user = user;
    }
    public void removeUserInternal() {
        this.user = null;
    }

    /**
     * update 메서드
     */
    public void updatePost(String title, String content, Category category) {
        if (title != null) {
            this.title = title;
        }

        if (content != null) {
            this.content = content;
        }
         if (category != null) {
             this.category = category;
         }
    }

    /**
     * Soft Delete 메서드
     */
    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }

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
