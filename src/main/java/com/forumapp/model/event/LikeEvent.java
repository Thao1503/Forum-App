package com.forumapp.model.event;

import com.forumapp.entity.CommentEntity;
import com.forumapp.entity.LikeEntity;
import com.forumapp.entity.PostEntity;
import com.forumapp.entity.UserEntity;

public record LikeEvent(UserEntity user,
                        LikeEntity like) {
}
