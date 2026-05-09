package com.forumapp.repository;

import java.util.List;
import java.util.Optional;
import com.forumapp.entity.UserEntity;
import com.forumapp.model.repository.PostStats;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    Optional<UserEntity> findByUsernameOrEmail(String username, String email);

    Optional<UserEntity> findByEmail(String email);
    Optional<UserEntity> findByUsername(String username);

    @Query("SELECT COUNT(u.id) AS totalMembers FROM UserEntity u")
    PostStats getAllUser();

//    UserEntity findByEmailOrUsername(String text);

    Page<UserEntity> findAll(Specification<UserEntity> list, Pageable pageable);




}
