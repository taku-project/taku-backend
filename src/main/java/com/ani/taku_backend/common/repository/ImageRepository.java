package com.ani.taku_backend.common.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ani.taku_backend.common.model.entity.Image;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ImageRepository extends JpaRepository<Image, Long> {

    @Query("select i.fileName from Image i join CommunityImage ci on i.id = ci.image.id where ci.post.id = :postId")
    List<String> findFileNamesByPostId(@Param("postId") Long postId);

    @Modifying
    @Query("update Image i set i.deletedAt = current_timestamp where i.fileName in :fileNames")
    void softDeleteByFileNames(@Param("fileNames") List<String> fileNames);

    @Query("select i from Image i join JangterImages ji on i.id = ji.image.id where ji.duckuJangter.id = :productId order by i.id asc")
    List<Image> findImageByproductId(@Param("productId") Long productId);

    List<Image> findByFileNameIn(List<String> fileNameList);
}
