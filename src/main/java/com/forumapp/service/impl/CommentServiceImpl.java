package com.forumapp.service.impl;

import com.forumapp.entity.*;
import com.forumapp.mapper.CommentMapper;
import com.forumapp.model.event.CommentEvent;
import com.forumapp.model.request.CommentRequest;
import com.forumapp.model.response.CategoryResponse;
import com.forumapp.model.response.CommentResponse;
import com.forumapp.model.response.CommentWsDto;
import com.forumapp.model.response.NotificationWsDto;
import com.forumapp.repository.*;
import com.forumapp.security.UserPrincipal;
import com.forumapp.service.CommentService;
import com.forumapp.service.UtilsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CommentMapper commentMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public List<CommentResponse> findTop5CommentNewest(){

        return commentRepository.findLatestComments(PageRequest.of(0, 5)).stream().map(comment -> CommentResponse.builder()
                .id(comment.getId())
                .avatar(comment.getUser().getProfile().getAvatar())
                .username(comment.getUser().getUsername())
                .title(comment.getPost().getTitle())
                .slug(comment.getSlug())
                .content(comment.getContent())
                .subCategory(CategoryResponse.builder()
                        .id(comment.getPost().getCategory().getId())
                        .slug(comment.getPost().getCategory().getSlug())
                        .name(comment.getPost().getCategory().getName())
                        .build())
                .category(CategoryResponse.builder()
                        .id(comment.getPost().getCategory().getParent().getId())
                        .slug(comment.getPost().getCategory().getParent().getSlug())
                        .name(comment.getPost().getCategory().getParent().getName())
                        .build())
                .createAt(comment.getCreatedAt())
                .build()).collect(Collectors.toList());
    }


    @Override
    @Transactional
    public CommentResponse commentPost(String slug, CommentRequest request) {
        PostEntity post = postRepository.findBySlug(slug);
        if (post == null) {
            throw new RuntimeException("Bài viết không tồn tại");
        }
        if (request.getQuotedCommentId() != null && request.getQuotedPostId() != null) {
            throw new RuntimeException("Chỉ được trích dẫn bài viết hoặc bình luận, không đồng thời cả hai");
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof UserPrincipal up)) {
            throw new RuntimeException("Cần đăng nhập");
        }
        UserEntity user = userRepository.findById(up.getId()).orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));

        CommentEntity comment = commentMapper.toEntity(request);
        comment.setUser(user);
        comment.setPost(post);


        if (request.getQuotedCommentId() != null) {
            CommentEntity quoted = commentRepository.findById(request.getQuotedCommentId())
                    .orElseThrow(() -> new RuntimeException("Bình luận được trích dẫn không tồn tại"));

            if (!quoted.getPost().getId().equals(post.getId())) {
                throw new RuntimeException("Bình luận được trích dẫn không thuộc bài viết này");
            }
            comment.setQuoted(quoted.getId());
        }

        if (request.getQuotedPostId() != null) {
            PostEntity quotedPost = postRepository.findById(request.getQuotedPostId())
                    .orElseThrow(() -> new RuntimeException("Bài viết được trích dẫn không tồn tại"));
            if (!quotedPost.getId().equals(post.getId())) {
                throw new RuntimeException("Bài viết được trích dẫn không hợp lệ");
            }
            comment.setQuotedPost(quotedPost.getId());
        }
        commentRepository.saveAndFlush(comment);

        comment.setSlug(post.getSlug() + "#post-" + comment.getId());
        commentRepository.save(comment);

        post.setReplies(post.getReplies() + 1);
        postRepository.save(post);

        eventPublisher.publishEvent(new CommentEvent(user, comment, post.getSlug()));
        return CommentResponse.builder()
                .id(comment.getId())
                .avatar(comment.getUser().getProfile().getAvatar())
                .username(comment.getUser().getUsername())
                .content(comment.getContent())
                .createAt(comment.getCreatedAt())
                .build();


    }


    @Override
    @Transactional
    public CommentResponse editComment(Long id, String content){
        CommentEntity comment = commentRepository.findById(id).orElseThrow(() -> new RuntimeException("Comment không tồn tại"));
        comment.setContent(content);
        commentRepository.save(comment);
        return CommentResponse.builder()
                .id(comment.getId())
                .avatar(comment.getUser().getProfile().getAvatar())
                .username(comment.getUser().getUsername())
                .content(comment.getContent())
                .slug(comment.getSlug())
                .updateAt(comment.getUpdatedAt())
                .build();
    }


    @Override
    @Transactional
    public void deleteComment(Long id){
        commentRepository.deleteById(id);
    }

}
