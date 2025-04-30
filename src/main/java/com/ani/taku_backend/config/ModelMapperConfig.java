package com.ani.taku_backend.config;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.ani.taku_backend.category.domain.dto.CategoryResDTO;
import com.ani.taku_backend.category.domain.entity.AnimationGenre;
import com.ani.taku_backend.category.domain.entity.Category;
import com.ani.taku_backend.category.domain.entity.CategoryImage;
import com.ani.taku_backend.category.domain.entity.CategoryGenre;
import com.ani.taku_backend.category.domain.dto.CategorySeachResDTO;
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
        modelMapper.createTypeMap(Category.class, CategoryResDTO.class)
            .addMappings(mapper -> {
                mapper.map(Category::getId, CategoryResDTO::setId);
                mapper.map(Category::getName, CategoryResDTO::setName);
                mapper.map(Category::getStatus, CategoryResDTO::setStatus);
                mapper.map(Category::getCreatedType, CategoryResDTO::setCreatedType);
                mapper.map(Category::getViewCount, CategoryResDTO::setViewCount);
            });

        // CategoryImage -> CategoryImageDTO 매핑
        modelMapper.createTypeMap(CategoryImage.class, CategoryResDTO.CategoryImageDTO.class)
            .addMapping(CategoryImage::getId, CategoryResDTO.CategoryImageDTO::setId)
            .addMapping(src -> src.getImage().getImageUrl(), CategoryResDTO.CategoryImageDTO::setImageUrl)
            .addMapping(src -> src.getImage().getFileName(), CategoryResDTO.CategoryImageDTO::setFileName)
            .addMapping(src -> src.getImage().getOriginalName(), CategoryResDTO.CategoryImageDTO::setOriginalFileName);

        // CategoryGenre -> CategoryGenreDTO 매핑
        modelMapper.createTypeMap(CategoryGenre.class, CategoryResDTO.CategoryGenreDTO.class)
            .addMapping(CategoryGenre::getId, CategoryResDTO.CategoryGenreDTO::setId)
            .addMappings(mapper -> {
                mapper.<String>map(
                    src -> src.getGenre().getGenreName(),
                    (dest, v) -> dest.setName(v)
                );
            });
    }

    // Category, CategoryGenre, CategoryImage -> ResponseCategorySeachDTO 매핑
    private void mapResponseCategorySeachDTO(ModelMapper modelMapper) {
        modelMapper.createTypeMap(Category.class, CategorySeachResDTO.class)
        .setProvider(req -> CategorySeachResDTO.builder().build())
        .addMappings(mapper -> {
            mapper.map(Category::getId, CategorySeachResDTO::setId);
            mapper.map(Category::getName, CategorySeachResDTO::setName);
            mapper.map(Category::getCreatedAt, CategorySeachResDTO::setCreatedAt);
            mapper.map(Category::getUpdatedAt, CategorySeachResDTO::setUpdatedAt);
            mapper.map(Category::getStatus, CategorySeachResDTO::setStatus);
            mapper.map(Category::getViewCount, CategorySeachResDTO::setViewCount);
            
            // User 관련 매핑
            mapper.<Long>map(
                src -> src.getUser().getUserId(),
                CategorySeachResDTO::setCategoryCreateUserId
            );
            mapper.<String>map(
                src -> src.getUser().getNickname(),
                CategorySeachResDTO::setCategoryCreateNickname
            );
            mapper.<String>map(
                src -> src.getUser().getProfileImg(),
                CategorySeachResDTO::setCategoryCreateUserProfileImageUrl
            );
        });

    // CategoryGenre 리스트에 대한 TypeMap
    modelMapper.typeMap(ArrayList.class, CategorySeachResDTO.class, "genreMapping")
        .setConverter(context -> {
            @SuppressWarnings("unchecked")
            List<CategoryGenre> genres = (List<CategoryGenre>) context.getSource();
            CategorySeachResDTO dto = context.getDestination();
            if (genres != null && !genres.isEmpty()) {
                dto.setGenreId(genres.stream().map(CategoryGenre::getGenre).map(AnimationGenre::getId).toArray(Long[]::new));
                dto.setGenreName(genres.stream().map(CategoryGenre::getGenre).map(AnimationGenre::getGenreName).toArray(String[]::new));
            }
            return dto;
        });

    // CategoryImage 리스트에 대한 TypeMap
    modelMapper.typeMap(ArrayList.class, CategorySeachResDTO.class, "imageMapping")
        .setConverter(context -> {
            @SuppressWarnings("unchecked")
            List<CategoryImage> images = (List<CategoryImage>) context.getSource();
            CategorySeachResDTO dto = context.getDestination();
            if (images != null && !images.isEmpty()) {
                CategoryImage firstImage = images.get(0);
                dto.setImageId(firstImage.getImage().getId());
                dto.setImageUrl(firstImage.getImage().getImageUrl());
            }
            return dto;
        });
    }


}
