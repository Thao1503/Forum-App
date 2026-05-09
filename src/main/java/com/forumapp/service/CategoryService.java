package com.forumapp.service;

import com.forumapp.model.response.CategoryResponse;
import com.forumapp.model.response.SubCategoryResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CategoryService {
    List<CategoryResponse> findAllCategory();
    SubCategoryResponse getDetailCategory(String slug, Pageable pageable);
//    void createNewCategory(String choice);
}
