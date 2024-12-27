package com.ani.taku_backend.admin.domain.entity;

import com.ani.taku_backend.admin.domain.dto.RequestUpdateProfanityDTO;
import com.ani.taku_backend.common.baseEntity.BaseTimeEntity;
import com.ani.taku_backend.common.enums.StatusType;
import com.ani.taku_backend.user.model.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "profanity_filter")
public class ProfanityFilter extends BaseTimeEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id")
    private User admin;

    @Column(name = "keyword")
    private String keyword;

    @Column(name = "explaination")
    private String explaination;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "is_active")
    private StatusType status;

    public void update(RequestUpdateProfanityDTO requestUpdateProfanityDTO) {
        if (requestUpdateProfanityDTO.getKeyword() != null) {
            this.keyword = requestUpdateProfanityDTO.getKeyword();
        }
        if (requestUpdateProfanityDTO.getExplaination() != null) {
            this.explaination = requestUpdateProfanityDTO.getExplaination();
        }

        this.status = requestUpdateProfanityDTO.getStatus();
    }
}
