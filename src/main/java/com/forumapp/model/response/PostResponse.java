package com.forumapp.model.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.forumapp.common.enums.RankStatus;
import com.forumapp.entity.FollowPostEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PostResponse {
    private Long id;
    private String title;
    private String content;
    private String username;
    private String avatar;
    private String slug;
    private Boolean locked;
    private RankStatus rank;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    private OffsetDateTime createdAt;

    private Long totalReplies;
    private Long totalViews;

    private Page<CommentResponse> comments;

    private List<FollowResponse> followPost;

    private Page<LikeResponse> likes;

}
