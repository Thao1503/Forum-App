package com.forumapp.repository;

import com.forumapp.entity.LikeEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LikeRepository extends JpaRepository<LikeEntity, Long> {

    Page<LikeEntity> findByPost_Id(Long postId, Pageable pageable);
    Page<LikeEntity> findByComment_Id(Long commentId, Pageable pageable);

}
