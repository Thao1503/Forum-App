package com.forumapp.repository;

import com.forumapp.entity.CommentEntity;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<CommentEntity, Long> {

    @Query("""
  SELECT c
  FROM CommentEntity c
  JOIN c.post p
  WHERE p.hide <> true
  ORDER BY c.createdAt DESC
""")
    List<CommentEntity> findLatestComments(Pageable pageable);

    Page<CommentEntity> findByPostId(Long id, Pageable pageable);


}