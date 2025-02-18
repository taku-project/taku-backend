package com.ani.taku_backend.jangter.model.entity;

import com.ani.taku_backend.common.baseEntity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Builder
@Entity
@Table(name = "item_categories")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ItemCategories extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_category_id")
    private Long id;

    private String name;
}
