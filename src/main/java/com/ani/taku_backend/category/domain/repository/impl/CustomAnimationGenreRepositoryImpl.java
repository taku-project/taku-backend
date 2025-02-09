package com.ani.taku_backend.category.domain.repository.impl;

import com.ani.taku_backend.category.domain.dto.AniGenreResDTO;
import com.ani.taku_backend.category.domain.dto.QAniGenreResDTO;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.ani.taku_backend.category.domain.entity.QAnimationGenre.animationGenre;

@Repository
@RequiredArgsConstructor
public class CustomAnimationGenreRepositoryImpl implements CustomAnimationGenreRepository {
    private final JPAQueryFactory queryFactory;

    @Override
    public List<AniGenreResDTO> findByGenreName(String keyword) {
        List<AniGenreResDTO> aniGenreDTO =
                queryFactory.select(
                    new QAniGenreResDTO(
                        animationGenre.id,
                        animationGenre.genreName
                    )
                ).from(animationGenre)
                .where(animationGenre.genreName.like(keyword + "%"))
                .fetch();

        return Optional.ofNullable(aniGenreDTO).orElse(new ArrayList<>());
    }
}
