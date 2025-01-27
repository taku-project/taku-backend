package com.ani.taku_backend.user.model.entity;

import com.ani.taku_backend.common.enums.UserRole;
import com.ani.taku_backend.post.model.entity.Post;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 유저 엔티티
 */
@Builder
@Entity
@Table(name = "Users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@ToString(exclude = {"posts"})  // 순환참조 방지
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "nickname", length = 255)
    private String nickname;              // 닉네임

    @Column(name = "provider_type", length = 50)
    private String providerType;          // 소셜로그인 타입 (예: KAKAO, NAVER 등)

    @Column(name = "profile_img", columnDefinition = "TEXT")
    private String profileImg;            // 프로필이미지 URL

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 10)
    private UserStatus status;                // 유저 상태 (예: ACTIVE, INACTIVE)

    @Column(name = "domestic_id", length = 255)
    private String domesticId;            // 도메스틱ID

    @Column(name = "gender", length = 30)
    private String gender;                // 성별

    @Column(name = "ageRange", length = 7)
    private String ageRange;              // 연령대

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;      // 생성일

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;      // 수정일

    @Column(name = "role", length = 10)
    @Enumerated(EnumType.STRING)
    private UserRole role;                  // 사용자 역할 (예: USER, ADMIN)

    @Column(name = "email", length = 20)
    private String email;                 // email

    // Post 연관관계 매핑
    @OneToMany(mappedBy = "user")
    @Builder.Default    // ArrayList로 초기값 고정
    private List<Post> posts = new ArrayList<>();

}