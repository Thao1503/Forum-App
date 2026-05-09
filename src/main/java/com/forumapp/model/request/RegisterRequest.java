package com.forumapp.model.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {

    @Email(message = "Email không đúng định dạng")
    @NotBlank(message = "Email không được để trống")
    @Pattern(regexp = "^\\S+$", message = "Email không được chứa khoảng trắng")
    private String email;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])\\S{6,}$",
            message = "Mật khẩu phải từ 6 ký tự, bao gồm chữ hoa, chữ thường, số, ký tự đặc biệt.")
    private String password;

    @NotBlank(message = "Tên người dùng không được để trống")
    @Size(min = 3, message = "Tên người dùng phải có ít nhất 3 kí tự")
    @Pattern(regexp = "^\\S+$", message = "Tên người dùng không được chứa khoảng trắng")
    private String username;
}
