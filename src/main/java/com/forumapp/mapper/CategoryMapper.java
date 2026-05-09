package com.forumapp.mapper;

import com.forumapp.entity.CategoryEntity;
import com.forumapp.model.request.CategoryRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
//    @Mapping(target = "id", ignore = true);
    CategoryEntity toEntity(CategoryRequest request);
}
