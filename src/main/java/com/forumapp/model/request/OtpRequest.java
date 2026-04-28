package com.forumapp.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.NumberFormat;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OtpRequest {

    @NotBlank(message = "Tên người dùng không được để trống")
    private String email;

    @NotBlank(message = "OTP không hợp lệ")
    @NumberFormat
    private String otp;
}
