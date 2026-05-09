package com.forumapp.service.impl;

import com.forumapp.model.response.CommentWsDto;
import com.forumapp.model.response.NotificationResponse;
import com.forumapp.model.response.NotificationWsDto;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationRealtimeService {
    private final SimpMessagingTemplate messagingTemplate;

    public void pushToUser(String username, NotificationWsDto response){
        messagingTemplate.convertAndSendToUser(
                username,
                "/queue/notifications",
                response
        );
    }

    public void pushToPost(Long postId, CommentWsDto response){
        messagingTemplate.convertAndSend("/topic/posts/" + postId + "/comments", response);

    }
}
