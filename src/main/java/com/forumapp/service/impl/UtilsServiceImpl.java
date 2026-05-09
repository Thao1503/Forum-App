package com.forumapp.service.impl;

import com.forumapp.entity.NotificationEntity;
import com.forumapp.entity.UserEntity;
import com.forumapp.model.response.NotificationResponse;
import com.forumapp.repository.NotificationRepository;
import com.forumapp.service.UtilsService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
@AllArgsConstructor
public class UtilsServiceImpl implements UtilsService {

    private final NotificationRepository notificationRepository;

    public NotificationResponse notificationPost(UserEntity user,
                                                 String title,
                                                 OffsetDateTime date,
                                                 String slug){
        NotificationEntity tb = NotificationEntity.builder()
                .recipient(user)
                .linkUrl(slug)
                .message(user.getUsername() + " đã tương tác trong " + title)
                .createdAt(date)
                .build();
        notificationRepository.save(tb);

        return NotificationResponse.builder()
                .username(tb.getRecipient().getUsername())
                .avatar(tb.getRecipient().getProfile().getAvatar())
                .title(title)
                .date(date)
                .message(title)
                .slug(slug)
                .build();

    }
}
