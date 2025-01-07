package com.ani.taku_backend.common.repository;

import com.ani.taku_backend.common.model.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ImageRepository extends JpaRepository<Image, Long> {

    @Query("select i.fileName from Image i join CommunityImage ci on i.id = ci.image.id where ci.post.id = :postId")
    List<String> findFileNamesByPostId(@Param("postId") Long postId);

    @Modifying
    @Query("update Image i set i.deletedAt = current_timestamp where i.fileName in :fileNames")
    void softDeleteByFileNames(@Param("fileNames") List<String> fileNames);

    @Modifying
    @Query("update Image i set i.deletedAt = current_timestamp where i.id = :imageId")
    void softDeleteByImageId(@Param("imageId") Long imageId);


    Optional<Image> findByFileName(String filename);

    List<Image> findByImageUrlIn(List<String> imageUrlList);

}
