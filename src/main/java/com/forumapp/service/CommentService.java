package com.forumapp.service;

import com.forumapp.model.request.CommentRequest;
import com.forumapp.model.response.CommentResponse;

import java.util.List;

public interface CommentService {
    List<CommentResponse> findTop5CommentNewest();

    CommentResponse commentPost(String slug, CommentRequest request);
    CommentResponse editComment(Long id, String content);
    void deleteComment(Long id);


}
