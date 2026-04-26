package com.forumapp.mapper;

import com.forumapp.entity.UserEntity;
import com.forumapp.model.request.RegisterRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "verified", constant = "false")
    @Mapping(target = "status", constant = "INACTIVE")
    UserEntity toEntity(RegisterRequest request);
}
