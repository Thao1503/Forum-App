package com.forumapp.api;

import com.forumapp.model.response.ApiResponse;
import com.forumapp.model.response.CategoryResponse;
import com.forumapp.model.response.SubCategoryResponse;
import com.forumapp.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/category")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping("")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getAllCategory(){
        return ResponseEntity.ok(ApiResponse.<List<CategoryResponse>>builder()
                .status(200)
                .message("Danh sách tất cả thể loại")
                .data(categoryService.findAllCategory())
                .build());
    }

    @GetMapping("/{slug}")
    public ResponseEntity<ApiResponse<SubCategoryResponse>> getDetailCategory(
            @PathVariable String slug,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.<SubCategoryResponse>builder()
                .status(200)
                .message("Danh sách các bài đăng của")
                .data(categoryService.getDetailCategory(slug, pageable))
                .build());
    }

}
