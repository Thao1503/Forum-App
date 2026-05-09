package com.forumapp.mapper;

import com.forumapp.entity.PostEntity;
import com.forumapp.model.request.PostRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PostMapper {
    @Mapping(target = "id", ignore = true)
    PostEntity toEntity(PostRequest request);
}
