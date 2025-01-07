package com.ani.taku_backend.post.model.entity;

import com.ani.taku_backend.category.domain.entity.Category;
import com.ani.taku_backend.common.baseEntity.BaseTimeEntity;
import com.ani.taku_backend.post.model.dto.PostUpdateRequestDTO;
import com.ani.taku_backend.user.model.entity.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.BatchSize;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 커뮤니티 게시글 Entity
 */
@Slf4j
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
            log.info("게시글 제목 수정 전, 기존 제목: {}, 수정 제목: {}", this.title, updateTitle);
            this.title = updateTitle;
            log.info("게시글 제목 수정 후, 기존 제목: {}, 수정 제목: {}", this.title, updateTitle);
        }

        if (updateContent != null && !updateContent.equals(this.content)) {
            log.info("게시글 본문 수정 전, 기존 본문: {}, 수정 본문: {}", this.content, updateContent);
            this.content = updateContent;
            log.info("게시글 본문 수정 후, 기존 본문: {}, 수정 본문: {}", this.content, updateContent);
        }

        if (category != null && !category.equals(this.category)) {
            log.info("카테고리 수정 전, 기존 카테고리: {}, 수정 카테고리: {}", this.category.getId(), category.getId());
            this.category = category;
            log.info("카테고리 수정 후, 기존 카테고리: {}, 수정 카테고리: {}", this.category.getId(), category.getId());
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
