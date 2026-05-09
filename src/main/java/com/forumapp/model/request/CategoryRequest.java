package com.forumapp.model.request;

import com.forumapp.entity.CategoryEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CategoryRequest {
    private String name;
    private String slug;
    private String groupName;
    private Integer displayOrder;
    private CategoryEntity parent;
}
