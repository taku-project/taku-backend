package com.ani.taku_backend.category.domain.dto;


import com.querydsl.core.annotations.QueryProjection;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AniGenreResDTO {
    private Long id;
    private String genreName;

    @QueryProjection
    public AniGenreResDTO(Long id, String genreName) {
        this.genreName = genreName;
        this.id = id;
    }
}
