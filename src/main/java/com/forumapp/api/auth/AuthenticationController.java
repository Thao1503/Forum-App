package com.forumapp.api.auth;


import com.forumapp.model.request.PasswordRequest;
import com.forumapp.model.request.LoginRequest;
import com.forumapp.model.request.OtpRequest;
import com.forumapp.model.request.RegisterRequest;
import com.forumapp.model.response.ApiResponse;
import com.forumapp.service.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping(value = "api/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;


    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> register(@Valid @RequestBody RegisterRequest request){
        authenticationService.register(request);
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .status(200)
                .message("Vui lòng nhập OTP để xác minh tài khoản")
                .build());
    }

    @PostMapping("/verify-account")
    public ResponseEntity<ApiResponse<String>> verify(@Valid @RequestBody OtpRequest request) {
        authenticationService.verifyRegister(request);
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .status(201)
                .message("Đăng kí tài khoản thành công")
                .build());

    }


    @PostMapping("/resend-otp")
    public ResponseEntity<ApiResponse<String>> sendOtp(@RequestBody OtpRequest request){
        authenticationService.sendOtp(request);
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .status(200)
                .message("Mã OTP mới đã được gửi, vui lòng kiểm tra email")
                .build());
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<String>> forgotPassword(@RequestBody LoginRequest request){
        authenticationService.forgotPassword(request);
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .status(200)
                .message("Mã OTP đã được gửi, vui lòng kiểm tra email")
                .build());
    }

    @PostMapping("/forgot-password/verify-otp")
    public ResponseEntity<ApiResponse<String>> verifyOtpForgotPassword(@RequestBody OtpRequest request){
        authenticationService.verifyOtpForgotPassword(request);
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .status(200)
                .message("Vui lòng nhập mật khẩu mới")
                .build());
    }

    @PutMapping("/forgot-password/reset-password")
    public ResponseEntity<ApiResponse<String>> resetPassword(@RequestBody PasswordRequest request){
        authenticationService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .status(200)
                .message("Đổi mật khẩu thành công")
                .build());
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<String>> login(@RequestBody LoginRequest request){
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .status(200)
                .message("Đăng nhập thành công")
                .data(authenticationService.login(request))
                .build());
    }






}
