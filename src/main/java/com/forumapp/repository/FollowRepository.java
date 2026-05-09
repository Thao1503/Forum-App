package com.forumapp.repository;

import com.forumapp.entity.FollowPostEntity;
import com.forumapp.entity.PostEntity;
import com.forumapp.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FollowRepository extends JpaRepository<FollowPostEntity, Long> {
    List<FollowPostEntity> findByPost_IdAndUser_Id(Long id, Long id1);
    FollowPostEntity findOneByPost_IdAndUser_Id(Long id, Long id1);
    List<FollowPostEntity> findByPostId(Long id);
    List<FollowPostEntity> findByUser_Id(Long id);
}
