package com.forumapp.model.request;

import com.forumapp.entity.CommentEntity;
import com.forumapp.entity.PostEntity;
import com.forumapp.entity.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NotificationRequest {
    private UserEntity user;
    private UserEntity send;
    private PostEntity post;
    private CommentEntity comment;
    private OffsetDateTime date;
}
