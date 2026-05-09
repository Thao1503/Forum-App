package com.forumapp.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NotificationWsDto {
    private String title;
    private String message;
    private String slug;
    private OffsetDateTime createdAt;
    private String username;
}
