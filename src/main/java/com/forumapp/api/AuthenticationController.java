package com.forumapp.api;


import com.forumapp.model.request.PasswordRequest;
import com.forumapp.model.request.LoginRequest;
import com.forumapp.model.request.OtpRequest;
import com.forumapp.model.request.RegisterRequest;
import com.forumapp.model.response.ApiResponse;
import com.forumapp.model.response.LoginResponse;
import com.forumapp.service.AuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("api/auth")
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
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .status(200)
                .message("Mã OTP đã được gửi, vui lòng kiểm tra email")
                .data(authenticationService.forgotPassword(request))
                .build());
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<String>> verifyOtpForgotPassword(@RequestBody OtpRequest request){
        authenticationService.verifyOtpForgotPassword(request);
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .status(200)
                .message("Vui lòng nhập mật khẩu mới")
                .build());
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<String>> resetPassword(@Valid @RequestBody PasswordRequest request){
        authenticationService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .status(200)
                .message("Đổi mật khẩu thành công")
                .build());
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @RequestBody LoginRequest request,
            HttpServletResponse response){
        LoginResponse data = authenticationService.login(request);
        ResponseCookie cookie = ResponseCookie.from("refresh_token", data.getRefreshToken())
                .httpOnly(true)
                .secure(false)
                .path("/api/auth")
                .maxAge(30L * 24 * 60 * 60)
                .sameSite("Lax")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        data.setRefreshToken(null);

        return ResponseEntity.ok(ApiResponse.<LoginResponse>builder()
                .status(200)
                .message("Đăng nhập thành công")
                .data(data)
                .build());
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = null;
        if (request.getCookies() != null) {
            for (var c : request.getCookies()) {
                if ("refresh_token".equals(c.getName())) {
                    refreshToken = c.getValue();
                    break;
                }
            }
        }

        authenticationService.logout(request, refreshToken);

        ResponseCookie deleteCookie = ResponseCookie.from("refresh_token", "")
                .httpOnly(true)
                .secure(false)
                .path("/api/auth")
                .maxAge(0)
                .sameSite("Lax")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, deleteCookie.toString());

        return ResponseEntity.ok(ApiResponse.<String>builder()
                .status(200)
                .message("Đăng xuất thành công")
                .data("Token đã bị vô hiệu hóa")
                .build());
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<LoginResponse>> refresh(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = null;
        if (request.getCookies() != null) {
            for (var c : request.getCookies()) {
                if ("refresh_token".equals(c.getName())) {
                    refreshToken = c.getValue();
                    break;
                }
            }
        }

        LoginResponse data = authenticationService.refreshAccessToken(refreshToken);

        ResponseCookie cookie = ResponseCookie.from("refresh_token", data.getRefreshToken())
                .httpOnly(true)
                .secure(false)
                .path("/api/auth")
                .maxAge(30L * 24 * 60 * 60)
                .sameSite("Lax")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        data.setRefreshToken(null);

        return ResponseEntity.ok(ApiResponse.<LoginResponse>builder()
                .status(200)
                .message("Làm mới token thành công")
                .data(data)
                .build());
    }





}
