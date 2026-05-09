package com.forumapp.repository;

import com.forumapp.entity.CategoryEntity;
import com.forumapp.model.response.ListPostResponse;
import com.forumapp.model.response.SubCategoryResponse;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<CategoryEntity, Long> {


    List<CategoryEntity> findByParentIsNull();
    CategoryEntity findBySlug(String slug);
    CategoryEntity findByPosts_Slug(String slug);


}
