package com.forumapp.repository;

import com.forumapp.entity.NotificationEntity;
import com.forumapp.entity.UserEntity;
import com.forumapp.model.response.NotificationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {
//    List<NotificationEntity> findByPost_id(Long id);
//    @Query("SELECT fpe FROM NotificationEntity fpe WHERE fpe.recipient.user_id = userId")
    Page<NotificationEntity> findByRecipientId(Long userId, Pageable pageable);
    List<NotificationEntity> findAllByRecipientId(Long id);
    void deleteByRecipientId(Long userId);



}
