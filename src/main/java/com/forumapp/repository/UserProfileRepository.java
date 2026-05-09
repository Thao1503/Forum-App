package com.forumapp.repository;

import com.forumapp.entity.UserProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfileEntity, Long> {
    UserProfileEntity findByUserId(Long id);

}
