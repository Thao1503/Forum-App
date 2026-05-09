package com.forumapp.model.response;

import com.forumapp.entity.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StatisticResponse {
    private Long totalThread;
    private Long totalReply;
    private Long totalMember;
}
