package com.forumapp.service.impl;

import com.forumapp.entity.CategoryEntity;
import com.forumapp.entity.PostEntity;
import com.forumapp.exception.ResourceNotFoundException;
import com.forumapp.mapper.CategoryMapper;
import com.forumapp.model.request.CategoryRequest;
import com.forumapp.model.response.CategoryResponse;
import com.forumapp.model.response.ListPostResponse;
import com.forumapp.model.response.SubCategoryResponse;
import com.forumapp.repository.CategoryRepository;
import com.forumapp.repository.PostRepository;
import com.forumapp.service.CategoryService;
import com.forumapp.utils.SlugUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final SlugUtils slugUtils;
    private final CategoryMapper categoryMapper;
    private final PostRepository postRepository;

    @Override
    public List<CategoryResponse> findAllCategory(){
        return categoryRepository.findByParentIsNull().stream().map(parent -> {
            List<SubCategoryResponse> childrenDto = parent.getChildren().stream()
                    .map(child -> SubCategoryResponse.builder()
                            .id(child.getId())
                            .name(child.getName())
                            .slug(child.getSlug())
                            .threadCount(child.getThreadCount())
                            .messageCount(child.getMessageCount())
                            .build()).collect(Collectors.toList());

            return CategoryResponse.builder()
                    .id(parent.getId())
                    .name(parent.getName())
                    .slug(parent.getSlug())
                    .children(childrenDto)
                    .build();
        }).collect(Collectors.toList());
    }

    @Override
    public SubCategoryResponse getDetailCategory(String slug, Pageable pageable){
       CategoryEntity category = categoryRepository.findBySlug(slug);
       Page<PostEntity> list = postRepository.findByCategory(category, pageable);
       Page<ListPostResponse> listPostDto = list.map(post -> ListPostResponse.builder()
               .id(post.getId())
               .title(post.getTitle())
               .slug(post.getSlug())
               .avatar(post.getAuthor().getProfile().getAvatar())
               .username(post.getAuthor().getUsername())
               .hide(post.getHide() == false)
               .locked(post.getLocked())
               .dateCreate(post.getCreatedAt())
               .totalView(post.getViews())
               .totalReply(post.getReplies())
               .build());
        return SubCategoryResponse.builder()
                .slug(category.getSlug())
                .posts(listPostDto)
                .build();
    }

//    @Override
//    @Transactional
//    public void createNewCategory(String choice, CategoryRequest request){
//        if(choice.equals("BIG")){
//            CategoryEntity category = categoryMapper.toEntity(request);
//
//        }
//    }
}
