package com.forumapp.service.impl;

import com.forumapp.entity.*;
import com.forumapp.mapper.CommentMapper;
import com.forumapp.model.request.CommentRequest;
import com.forumapp.model.response.CommentResponse;
import com.forumapp.model.response.CommentWsDto;
import com.forumapp.model.response.NotificationWsDto;
import com.forumapp.repository.*;
import com.forumapp.security.UserPrincipal;
import com.forumapp.service.CommentService;
import com.forumapp.service.UtilsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
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
    private final FollowRepository followRepository;
    private final UtilsService utilsService;
    private final NotificationRealtimeService notificationRealtimeService;
    private final CategoryRepository categoryRepository;
    private final UserProfileRepository userProfileRepository;

    @Override
    public List<CommentResponse> findTop5CommentNewest(){
        return commentRepository.findLatestComments(PageRequest.of(0, 5)).stream().map(comment -> CommentResponse.builder()
                .id(comment.getId())
                .avatar(comment.getUser().getProfile().getAvatar())
                .username(comment.getUser().getUsername())
                .title(comment.getPost().getTitle())
                .slug(comment.getSlug())
                .content(comment.getContent())
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

            UserEntity repUser = userRepository.findById(quoted.getUser().getId()).orElseThrow(() -> new RuntimeException("Người bình luận không tồn tại"));

            comment.setQuoted(quoted.getId());
            utilsService.notificationPost(repUser, post.getTitle(),quoted.getCreatedAt(),quoted.getSlug());
            notificationRealtimeService.pushToUser(
                    repUser.getUsername(),
                    NotificationWsDto.builder()
                            .title("Có người phản hồi bình luận của bạn")
                            .message(user.getUsername() + " đã bình luận")
                            .slug(quoted.getSlug())
                            .createdAt(quoted.getCreatedAt())
                            .username(user.getUsername())
                            .build()
            );
        }

        if (request.getQuotedPostId() != null) {
            PostEntity quotedPost = postRepository.findById(request.getQuotedPostId())
                    .orElseThrow(() -> new RuntimeException("Bài viết được trích dẫn không tồn tại"));
            if (!quotedPost.getId().equals(post.getId())) {
                throw new RuntimeException("Bài viết được trích dẫn không hợp lệ");
            }
            comment.setQuotedPost(quotedPost.getId());
        }
            commentRepository.save(comment);

            comment.setSlug(post.getSlug() + "#post-" + comment.getId());
            commentRepository.save(comment);

        notificationRealtimeService.pushToPost(
                post.getId(),
                CommentWsDto.builder()
                        .type("COMMENT_CREATED")
                        .postId(post.getId())
                        .commentId(comment.getId())
                        .slug(comment.getSlug())
                        .content(comment.getContent())
                        .createdAt(comment.getCreatedAt())
                        .username(user.getUsername())
                        .avatar(comment.getUser().getProfile() != null ? comment.getUser().getProfile().getAvatar() : null)
                        .build()
        );

            Long count = post.getReplies() + 1;
            post.setReplies(count);
            postRepository.save(post);

        UserProfileEntity profile = userProfileRepository.findByUserId(up.getId());
        Long point = profile.getPoints() + 1L;
        profile.setPoints(point);
        userProfileRepository.save(profile);



            CategoryEntity category = categoryRepository.findByPosts_Slug(slug);
            Long count2 = category.getMessageCount() + 1L;
            category.setMessageCount(count2);
            categoryRepository.save(category);



            List<FollowPostEntity> followers = followRepository.findByPostId(post.getId());


            for (FollowPostEntity f : followers) {
                if (!Boolean.TRUE.equals(f.getChecked())) continue;
                if (f.getUser() == null || f.getUser().getId().equals(user.getId())) continue;
                utilsService.notificationPost(f.getUser(), post.getTitle(), comment.getCreatedAt(), comment.getSlug());
                notificationRealtimeService.pushToUser(
                        f.getUser().getUsername(),
                        NotificationWsDto.builder()
                                .title("Bài viết của bạn có bình luận mới")
                                .message(user.getUsername() + " đã bình luận")
                                .slug(comment.getSlug())
                                .createdAt(comment.getCreatedAt())
                                .username(user.getUsername())
                                .build()
                );
            }


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
