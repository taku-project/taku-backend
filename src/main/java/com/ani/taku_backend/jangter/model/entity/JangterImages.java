package com.ani.taku_backend.jangter.model.entity;

import com.ani.taku_backend.common.model.entity.Image;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Builder
@Entity
@Table(name = "jangter_images")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class JangterImages {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private DuckuJangter duckuJangter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "images_id")
    private Image image;

    public void addDuckuJangter(DuckuJangter duckuJangter) {
        this.duckuJangter = duckuJangter;
    }
}
