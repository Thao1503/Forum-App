package com.forumapp.model.event;

import com.forumapp.entity.UserEntity;

public record UserVerifiedEvent(UserEntity user) {
}
