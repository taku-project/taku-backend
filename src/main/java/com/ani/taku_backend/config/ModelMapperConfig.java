package com.ani.taku_backend.config;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.ani.taku_backend.category.domain.dto.ResponseCategoryDTO;
import com.ani.taku_backend.category.domain.entity.Category;
import com.ani.taku_backend.category.domain.entity.CategoryImage;
import com.ani.taku_backend.category.domain.entity.CategoryGenre;

@Configuration
public class ModelMapperConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        this.configure(modelMapper);
        this.mapCategory(modelMapper);
        return modelMapper;
    }

    public void configure(ModelMapper modelMapper) {
                // 기본 설정
        modelMapper.getConfiguration()
            .setMatchingStrategy(MatchingStrategies.STRICT)
            .setSkipNullEnabled(true);
            

    }

    private void mapCategory(ModelMapper modelMapper) {
        // 기본 설정
        modelMapper.getConfiguration()
            .setMatchingStrategy(MatchingStrategies.STRICT)
            .setFieldMatchingEnabled(true)
            .setFieldAccessLevel(org.modelmapper.config.Configuration.AccessLevel.PRIVATE);

        // Category -> ResponseCategoryDTO 매핑
        modelMapper.createTypeMap(Category.class, ResponseCategoryDTO.class)
            .addMappings(mapper -> {
                mapper.map(Category::getId, ResponseCategoryDTO::setId);
                mapper.map(Category::getName, ResponseCategoryDTO::setName);
                mapper.map(Category::getStatus, ResponseCategoryDTO::setStatus);
                mapper.map(Category::getCreatedType, ResponseCategoryDTO::setCreatedType);
                mapper.map(Category::getViewCount, ResponseCategoryDTO::setViewCount);
            });

        // CategoryImage -> CategoryImageDTO 매핑
        modelMapper.createTypeMap(CategoryImage.class, ResponseCategoryDTO.CategoryImageDTO.class)
            .addMapping(CategoryImage::getId, ResponseCategoryDTO.CategoryImageDTO::setId)
            .addMappings(mapper -> {
                mapper.<String>map(
                    src -> src.getImage().getImageUrl(),
                    (dest, v) -> dest.setImageUrl(v)
                );
                mapper.<String>map(
                    src -> src.getImage().getFileName(),
                    (dest, v) -> dest.setFileName(v)
                );
                mapper.<String>map(
                    src -> src.getImage().getOriginalName(),
                    (dest, v) -> dest.setOriginalFileName(v)
                );
            });

        // CategoryGenre -> CategoryGenreDTO 매핑
        modelMapper.createTypeMap(CategoryGenre.class, ResponseCategoryDTO.CategoryGenreDTO.class)
            .addMapping(CategoryGenre::getId, ResponseCategoryDTO.CategoryGenreDTO::setId)
            .addMappings(mapper -> {
                mapper.<String>map(
                    src -> src.getGenre().getGenreName(),
                    (dest, v) -> dest.setName(v)
                );
            });
    }
}
