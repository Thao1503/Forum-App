package com.forumapp.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommentWsDto {
    private String type;
    private Long postId;
    private Long commentId;
    private String slug;
    private String content;
    private OffsetDateTime createdAt;
    private String username;
    private String avatar;
}
