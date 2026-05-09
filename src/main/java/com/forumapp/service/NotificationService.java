package com.forumapp.service;

import com.forumapp.entity.UserEntity;
import com.forumapp.model.response.CommentResponse;
import com.forumapp.model.response.LikeResponse;
import com.forumapp.model.response.NotificationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

public interface NotificationService {
    void changeFollowPost(String slug);


    Page<NotificationResponse> getNotification(Pageable pageable);

    void checkAllNotification();

    void deleteOneNotification(Long id);
    void deleteAllNotification();

    void likePost(Long id);
    void deleteLikePost(Long id);

    void likeComment(Long id);
    void deleteLikeComment(Long id);

    Page<LikeResponse> getAllLikePost(Long id, Pageable pageable);
    Page<LikeResponse> getAllLikeComment(Long id, Pageable pageable);
}
