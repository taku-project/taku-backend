package com.ani.taku_backend.post.model.entity;

import com.ani.taku_backend.common.model.entity.Image;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.Objects;

/**
 * 커뮤니티 이미지 Entity
 */
@Entity
@Table(name = "community_images")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class CommunityImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "image_id")
    private Image image;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    /**
     * 연관관계 편의 메서드
     */
    void assignPost(Post post) {
        this.post = post;
    }

}
