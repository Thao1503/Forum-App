package com.forumapp.model.listener;

import com.forumapp.entity.CommentEntity;
import com.forumapp.entity.PostEntity;
import com.forumapp.entity.UserEntity;
import com.forumapp.model.event.CommentEvent;
import com.forumapp.model.event.LikeEvent;
import com.forumapp.model.request.NotificationRequest;
import com.forumapp.model.response.NotificationWsDto;
import com.forumapp.service.UtilsService;
import com.forumapp.service.impl.NotificationRealtimeService;
import lombok.AllArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Component
@AllArgsConstructor
public class LikeEventListener {

    private final UtilsService utilsService;
    private final NotificationRealtimeService notificationRealtimeService;

    @Async
    @EventListener
    public void onHandleLike(LikeEvent event){
        UserEntity user = event.user();
        PostEntity post = event.like().getPost();
        CommentEntity comment = event.like().getComment();
        if(event.like().getPost().getId() != null){
            if(!post.getAuthor().getId().equals(user.getId())){
                OffsetDateTime date = OffsetDateTime.now();
                NotificationRequest request = NotificationRequest.builder()
                        .user(post.getAuthor())
                        .send(user)
                        .post(post)
                        .comment(comment)
                        .build();
                utilsService.notificationLike(request);
                notificationRealtimeService.pushToUser(
                        post.getAuthor().getUsername(),
                        NotificationWsDto.builder()
                                .title("Bài viết của bạn đã có người thích")
                                .message(user.getUsername() + " đã thích bài viết của bạn")
                                .slug(post.getSlug())
                                .createdAt(date)
                                .username(user.getUsername())
                                .build());
            }
        }
        if(event.like().getComment().getId() != null){
            if(!comment.getUser().getId().equals(user.getId())){
                OffsetDateTime date = OffsetDateTime.now();
                NotificationRequest request = NotificationRequest.builder()
                        .user(post.getAuthor())
                        .send(user)
                        .post(post)
                        .comment(comment)
                        .build();
                utilsService.notificationLike(request);
                notificationRealtimeService.pushToUser(
                        comment.getUser().getUsername(),
                        NotificationWsDto.builder()
                                .title("Bình luận của bạn đã có người thích")
                                .message(user.getUsername() + " đã thích bình luận của bạn")
                                .slug(comment.getContent())
                                .createdAt(date)
                                .username(user.getUsername())
                                .build());
            }
        }
    }
}
