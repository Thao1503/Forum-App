package com.forumapp.model.response;

import com.forumapp.common.enums.RankStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProfileResponse {
    private Long id;
    private String name;
    private String email;
    private Long point;
    private RankStatus rank;
    private String avatar;
    private String phone;
    private OffsetDateTime dob;
}
