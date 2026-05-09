package com.forumapp.repository;

import com.forumapp.entity.CategoryEntity;
import com.forumapp.entity.PostEntity;
import com.forumapp.entity.UserEntity;
import com.forumapp.model.repository.PostStats;
import com.forumapp.model.response.PostResponse;
import com.forumapp.model.response.StatisticResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<PostEntity, Long> {

    Page<PostEntity> findByCategory(CategoryEntity category, Pageable pageable);

    @Query("""
    SELECT p
    FROM PostEntity p
    JOIN FollowPostEntity fpe ON fpe.post = p
    WHERE fpe.user.id = :userId
      AND fpe.checked = true
      AND p.author.id <> :userId
              """)
    Page<PostEntity> findFollowedPosts(@Param("userId") Long userId, Pageable pageable);

    Page<PostEntity> findByAuthor_Id(Long id, Pageable pageable);


    Page<PostEntity> findAll(Specification<PostEntity> list, Pageable pageable);

    @Query("SELECT COUNT(p.id) AS totalThreads, SUM(p.replies) AS totalReplies FROM PostEntity p")
    PostStats getPostStatistics();


    PostEntity findBySlug(String slug);

    @Query("SELECT p FROM PostEntity p WHERE p.hide = false ORDER BY p.createdAt DESC")
    List<PostEntity> findTop5ByOrderByCreatedAtDesc(Pageable pageable);

    @Query("""
  SELECT p
  FROM PostEntity p
  JOIN p.author u
  JOIN u.roleEntity re
  JOIN p.category c
  WHERE re.name = :roleName
    AND c.groupName = :groupName
    AND p.hide <> true
  ORDER BY p.createdAt DESC
""")
    List<PostEntity> findPostsByRoleAndGroup(
            @Param("roleName") String roleName,
            @Param("groupName") String groupName,
            Pageable pageable
    );
}

