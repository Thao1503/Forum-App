package com.forumapp.model.response;

import com.forumapp.entity.CommentEntity;
import com.forumapp.entity.PostEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LikeResponse {
    private Long id;
    private Long userId;
    private String username;
    private Long postId;
    private Long commentId;
}
