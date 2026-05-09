package com.forumapp.service;

import com.forumapp.entity.PostEntity;
import com.forumapp.model.request.PostRequest;
import com.forumapp.model.response.PostResponse;
import com.forumapp.model.response.StatisticResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PostService {

    StatisticResponse getStatisticForum();

    List<PostResponse> findTop5PostNewest();

    Page<PostResponse> findByUserId(Pageable pageable);

    PostResponse createPost(String slug, PostRequest request);

    PostResponse getDetailPost(String slug, Pageable pageable, HttpServletRequest request, HttpServletResponse response);

    PostResponse editPost(Long id, PostRequest request, Pageable pageable);
    void deletePost(Long id);

    Page<PostResponse> getAllPostFollow(Pageable pageable);

    void lockedPost(Long id);

    List<PostResponse> findTop5PostByAdmin();




}
