package com.ani.taku_backend.post.repository;

import com.ani.taku_backend.post.model.dto.FindPostQueryDTO;
import com.ani.taku_backend.post.model.entity.Post;
import com.ani.taku_backend.post.repository.impl.PostRepositoryCustom;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<Post, Long>, PostRepositoryCustom {

}
