package com.forumapp.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PostRequest {

    @NotBlank
    private Long userId;

    @NotBlank(message = "Tiêu không được để trống")
    private String title;

    @NotBlank(message = "Nội dung không được để trống")
    private String content;
}
