package com.ani.taku_backend.config;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.ani.taku_backend.category.domain.dto.ResponseCategoryDTO;
import com.ani.taku_backend.category.domain.entity.AnimationGenre;
import com.ani.taku_backend.category.domain.entity.Category;
import com.ani.taku_backend.category.domain.entity.CategoryImage;
import com.ani.taku_backend.category.domain.entity.CategoryGenre;
import com.ani.taku_backend.category.domain.dto.ResponseCategorySeachDTO;
import org.modelmapper.Converter;
import java.util.List;
import java.util.ArrayList;

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

        this.mapResponseCategoryDTO(modelMapper);
        this.mapResponseCategorySeachDTO(modelMapper);
    }

    // Category, CategoryGenre, CategoryImage -> ResponseCategoryDTO 매핑
    private void mapResponseCategoryDTO(ModelMapper modelMapper) {
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

    // Category, CategoryGenre, CategoryImage -> ResponseCategorySeachDTO 매핑
    private void mapResponseCategorySeachDTO(ModelMapper modelMapper) {
        modelMapper.createTypeMap(Category.class, ResponseCategorySeachDTO.class)
        .setProvider(req -> ResponseCategorySeachDTO.builder().build())
        .addMappings(mapper -> {
            mapper.map(Category::getId, ResponseCategorySeachDTO::setId);
            mapper.map(Category::getName, ResponseCategorySeachDTO::setName);
            mapper.map(Category::getCreatedAt, ResponseCategorySeachDTO::setCreatedAt);
            mapper.map(Category::getUpdatedAt, ResponseCategorySeachDTO::setUpdatedAt);
            mapper.map(Category::getStatus, ResponseCategorySeachDTO::setStatus);
            mapper.map(Category::getViewCount, ResponseCategorySeachDTO::setViewCount);
            
            // User 관련 매핑
            mapper.<Long>map(
                src -> src.getUser().getUserId(),
                ResponseCategorySeachDTO::setCategoryCreateUserId
            );
            mapper.<String>map(
                src -> src.getUser().getNickname(),
                ResponseCategorySeachDTO::setCategoryCreateNickname
            );
            mapper.<String>map(
                src -> src.getUser().getProfileImg(),
                ResponseCategorySeachDTO::setCategoryCreateUserProfileImageUrl
            );
        });

    // CategoryGenre 리스트에 대한 TypeMap
    modelMapper.typeMap(ArrayList.class, ResponseCategorySeachDTO.class, "genreMapping")
        .setConverter(context -> {
            @SuppressWarnings("unchecked")
            List<CategoryGenre> genres = (List<CategoryGenre>) context.getSource();
            ResponseCategorySeachDTO dto = context.getDestination();
            if (genres != null && !genres.isEmpty()) {
                dto.setGenreId(genres.stream().map(CategoryGenre::getGenre).map(AnimationGenre::getId).toArray(Long[]::new));
                dto.setGenreName(genres.stream().map(CategoryGenre::getGenre).map(AnimationGenre::getGenreName).toArray(String[]::new));
            }
            return dto;
        });

    // CategoryImage 리스트에 대한 TypeMap
    modelMapper.typeMap(ArrayList.class, ResponseCategorySeachDTO.class, "imageMapping")
        .setConverter(context -> {
            @SuppressWarnings("unchecked")
            List<CategoryImage> images = (List<CategoryImage>) context.getSource();
            ResponseCategorySeachDTO dto = context.getDestination();
            if (images != null && !images.isEmpty()) {
                CategoryImage firstImage = images.get(0);
                dto.setImageId(firstImage.getImage().getId());
                dto.setImageUrl(firstImage.getImage().getImageUrl());
            }
            return dto;
        });
    }


}
