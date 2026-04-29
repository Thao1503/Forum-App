package com.forumapp.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PasswordRequest {

    private String email;

    private String otp;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])\\S{6,}$",
            message = "Mật khẩu phải từ 6 ký tự, bao gồm chữ hoa, chữ thường, số, ký tự đặc biệt và không có khoảng trắng")
    private String password;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])\\S{6,}$",
            message = "Mật khẩu phải từ 6 ký tự, bao gồm chữ hoa, chữ thường, số, ký tự đặc biệt và không có khoảng trắng")
    private String passwordConfirm;
}
