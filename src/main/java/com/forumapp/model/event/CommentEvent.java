package com.forumapp.model.event;

import com.forumapp.entity.CommentEntity;
import com.forumapp.entity.PostEntity;
import com.forumapp.entity.UserEntity;

public record CommentEvent(UserEntity user,
                           CommentEntity comment,
                           String categorySlug) {
}
