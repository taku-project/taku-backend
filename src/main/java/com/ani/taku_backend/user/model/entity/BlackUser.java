package com.ani.taku_backend.user.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "black_user")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class BlackUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;                    // 블랙리스트 대상 유저

    @Column(name = "admin_id")
    private String adminId;               // 관리자 ID

    @Column(name = "admin_name")
    private String adminName;             // 관리자 이름

    @Column(name = "reason")
    private String reason;                // 블랙리스트 사유

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;      // 등록일

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;      // 수정일
}
