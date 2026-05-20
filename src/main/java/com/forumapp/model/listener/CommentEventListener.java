package com.forumapp.model.listener;

import com.forumapp.entity.*;

import com.forumapp.model.event.CommentEvent;
import com.forumapp.model.request.NotificationRequest;
import com.forumapp.model.response.CommentWsDto;
import com.forumapp.model.response.NotificationWsDto;
import com.forumapp.repository.*;
import com.forumapp.service.UtilsService;
import com.forumapp.service.impl.NotificationRealtimeService;
import lombok.AllArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Component
@AllArgsConstructor
public class CommentEventListener {

    private final NotificationRealtimeService notificationRealtimeService;
    private final UserProfileRepository userProfileRepository;
    private final CategoryRepository categoryRepository;
    private final FollowRepository followRepository;
    private final CommentRepository commentRepository;
    private final UtilsService utilsService;

    @Async
    @EventListener
    @Transactional
    public void handleCommentPostHierarchy(CommentEvent event) {
        CommentEntity comment = event.comment();
        UserEntity user = comment.getUser();
        PostEntity post = comment.getPost();

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
                        .avatar(user.getProfile() != null ? user.getProfile().getAvatar() : null)
                        .build()
        );

        UserProfileEntity profile = userProfileRepository.findByUserId(user.getId());
        if (profile != null) {
            profile.setPoints(profile.getPoints() + 1L);
            userProfileRepository.save(profile);
        }

        CategoryEntity category = categoryRepository.findByPosts_Slug(event.categorySlug());
        if (category != null) {
            category.setMessageCount(category.getMessageCount() + 1L);
            categoryRepository.save(category);
        }

        if (comment.getQuoted() != null && !event.user().getId().equals(comment.getQuoted())) {
            NotificationRequest request = NotificationRequest.builder()
                    .user(user)
                    .send(event.user())
                    .post(post)
                    .comment(comment)
                    .date(comment.getCreatedAt())
                    .build();
                    utilsService.notificationPost(request);
                    notificationRealtimeService.pushToUser(
                            user.getUsername(),
                            NotificationWsDto.builder()
                                    .title("Có người phản hồi bình luận của bạn")
                                    .message(user.getUsername() + " đã bình luận")
                                    .slug(comment.getSlug())
                                    .createdAt(comment.getCreatedAt())
                                    .username(user.getUsername())
                                    .build()
                    );
        }

        List<FollowPostEntity> followers = followRepository.findByPostId(event.comment().getPost().getId());
        for (FollowPostEntity f : followers) {
            if (!Boolean.TRUE.equals(f.getChecked())) continue;
            if (f.getUser() == null || f.getUser().getId().equals(user.getId())) continue;

            NotificationRequest request2 = NotificationRequest.builder()
                    .user(f.getUser())
                    .send(user)
                    .post(post)
                    .comment(comment)
                    .date(comment.getCreatedAt())
                    .build();

            utilsService.notificationPost(request2);

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
    }
}