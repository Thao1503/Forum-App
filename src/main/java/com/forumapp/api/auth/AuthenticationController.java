package com.forumapp.api.auth;


import com.forumapp.entity.UserEntity;
import com.forumapp.model.request.RegisterRequest;
import com.forumapp.model.response.ApiResponse;
import com.forumapp.service.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<String>> verify(@RequestParam String email,
                                         @RequestParam String otp){
        boolean success = authenticationService.verifyRegister(email, otp);
        if(success){
            return ResponseEntity.ok(ApiResponse.<String>builder()
                    .status(200)
                    .message("Đăng kí tài khoản thành công")
                    .build());
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.<String>builder()
                .status(400)
                .message("Mã OTP không đúng hoặc đã hết hạn")
                .build());
    }
}
