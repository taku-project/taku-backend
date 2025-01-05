package com.ani.taku_backend.post.model.entity;

import com.ani.taku_backend.category.domain.entity.Category;
import com.ani.taku_backend.common.baseEntity.BaseTimeEntity;
import com.ani.taku_backend.post.model.dto.PostUpdateRequestDTO;
import com.ani.taku_backend.user.model.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import java.time.LocalDateTime;
import java.util.ArrayList;
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

    @Column(length = 150, nullable = false)
    private String title;

    @Column(length = 3000, nullable = false)
    private String content;

    private long views;

    private LocalDateTime deletedAt ;

    /**
     * 커뮤니티이미지 연관관계 편의 메서드
     */
    public void addCommunityImage(CommunityImage communityImage) {
        this.communityImages.add(communityImage);
        communityImage.assignPost(this);
    }

    /**
     * update 메서드
     */
    public void updatePost(PostUpdateRequestDTO postUpdateRequestDTO, Category category) {
        String updateTitle = postUpdateRequestDTO.getTitle();
        String updateContent = postUpdateRequestDTO.getContent();

        if (updateTitle != null && !updateTitle.equals(this.title)) {
            this.title = updateTitle;
        }

        if (updateContent != null && !updateContent.equals(this.content)) {
            this.content = updateContent;
        }

        if (category != null && !category.equals(this.category)) {
            this.category = category;
        }
    }

    /**
     * Soft Delete 메서드
     */
    public void delete() {
        this.deletedAt = LocalDateTime.now();
    }

    // 조회수 증가
    public void addViews() {
        this.views++;
    }


}
