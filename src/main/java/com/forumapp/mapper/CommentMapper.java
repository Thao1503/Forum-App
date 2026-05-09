package com.forumapp.mapper;

import com.forumapp.entity.CommentEntity;
import com.forumapp.entity.PostEntity;
import com.forumapp.model.request.CommentRequest;
import com.forumapp.model.request.PostRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CommentMapper {
    @Mapping(target = "id", ignore = true)
    CommentEntity toEntity(CommentRequest request);
}
