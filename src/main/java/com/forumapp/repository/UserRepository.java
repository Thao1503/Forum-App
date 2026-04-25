package com.forumapp.repository;

import java.util.Optional;
import com.forumapp.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    boolean existsByEmail(String email);
    Optional<UserEntity> findByUsername(String username);

    Optional<UserEntity> findByEmail(String email);

}
