package com.forumapp.service;

import com.forumapp.entity.PostEntity;
import com.forumapp.entity.UserEntity;
import com.forumapp.model.response.CommentResponse;
import com.forumapp.model.response.NotificationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;

public interface UtilsService {

    NotificationResponse notificationPost(UserEntity user,
                                          String title,
                                          OffsetDateTime date,
                                          String slug);


}
