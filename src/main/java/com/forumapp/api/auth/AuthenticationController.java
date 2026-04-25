package com.forumapp.api.auth;


import com.forumapp.model.request.RegisterRequest;
import com.forumapp.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "api/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request){
        authenticationService.register(request);
        return ResponseEntity.ok("Đăng kí tài khoản thành công, vui lòng nhập OTP để xác minh tài khoản");
    }

    @PostMapping("/verify")
    public ResponseEntity<String> verify(@RequestParam String email,
                                         @RequestParam String otp){
        boolean success = authenticationService.verifyRegister(email, otp);
        if(success){
            return ResponseEntity.ok("Đăng kí tài khoản thành công");
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Mã OTP không đúng hoặc đã hết hạn.");
    }
}
