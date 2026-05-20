package com.forumapp.service.impl;

import com.forumapp.entity.CommentEntity;
import com.forumapp.entity.NotificationEntity;
import com.forumapp.entity.PostEntity;
import com.forumapp.entity.UserEntity;
import com.forumapp.model.request.CommentRequest;
import com.forumapp.model.request.NotificationRequest;
import com.forumapp.model.response.CommentWsDto;
import com.forumapp.model.response.NotificationResponse;
import com.forumapp.model.response.NotificationWsDto;
import com.forumapp.repository.CommentRepository;
import com.forumapp.repository.NotificationRepository;
import com.forumapp.repository.PostRepository;
import com.forumapp.repository.UserRepository;
import com.forumapp.service.UtilsService;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
@AllArgsConstructor
public class UtilsServiceImpl implements UtilsService {

    private final NotificationRepository notificationRepository;


    @Override
    public void notificationPost(NotificationRequest request){
        NotificationEntity tb = NotificationEntity.builder()
                .recipient(request.getUser())
                .linkUrl(request.getComment().getSlug())
                .message(request.getSend().getUsername() + " đã tương tác trong " + request.getPost().getTitle())
                .userSend(request.getSend().getId())
                .avatar(request.getSend().getProfile().getAvatar())
                .createdAt(request.getDate())
                .build();
        notificationRepository.save(tb);
    }

    @Override
    public void notificationLike(NotificationRequest request){
        if(request.getPost().getId() != null){
            NotificationEntity tb = NotificationEntity.builder()
                    .recipient(request.getUser())
                    .linkUrl(request.getPost().getSlug())
                    .message(request.getSend().getUsername() + " thích bài viết của bạn " + request.getPost().getTitle())
                    .userSend(request.getSend().getId())
                    .avatar(request.getSend().getProfile().getAvatar())
                    .createdAt(request.getDate())
                    .build();
            notificationRepository.save(tb);
        }
        else if(request.getComment().getId() != null){
            NotificationEntity tb = NotificationEntity.builder()
                    .recipient(request.getUser())
                    .linkUrl(request.getComment().getSlug())
                    .message(request.getSend().getUsername() + " đã thích bình luận của bạn ")
                    .userSend(request.getSend().getId())
                    .avatar(request.getSend().getProfile().getAvatar())
                    .createdAt(request.getDate())
                    .build();
            notificationRepository.save(tb);
        }

    }


}
