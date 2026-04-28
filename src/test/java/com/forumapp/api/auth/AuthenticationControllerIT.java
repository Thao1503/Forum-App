package com.forumapp.api.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest // Chạy toàn bộ Application Context để test Integration
@AutoConfigureMockMvc // Tự động cấu hình MockMvc để giả lập request
class AuthenticationControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String testEmail = "fresher.java@fpt.edu.vn";
    private String testPassword = "Password123@";

    @BeforeEach
    void setUp() {
        // Xóa dữ liệu cũ hoặc chuẩn bị môi trường sạch nếu cần
    }

    @Test
    void testFullAuthenticationFlow() throws Exception {
        // 1. GIAI ĐOẠN ĐĂNG KÝ (REGISTER)
        // Giả sử bạn có class RegisterRequest khớp với Backend
        var registerRequest = new Object() {
            public String email = testEmail;
            public String password = testPassword;
        };

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated()); // Kiểm tra status 201

        // 2. GIAI ĐOẠN XÁC THỰC (VERIFY OTP)
        // Trong môi trường test, bạn có thể fix cứng mã OTP là "123456"
        var otpRequest = new Object() {
            public String email = testEmail;
            public String otp = "123456";
        };

        mockMvc.perform(post("/api/auth/verify-account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(otpRequest)))
                .andExpect(status().isCreated());

        // 3. GIAI ĐOẠN ĐĂNG NHẬP (LOGIN)
        // Sử dụng class LoginRequest tương ứng
        var loginRequest = new Object() {
            public String identifier = testEmail;
            public String password = testPassword;
        };

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").exists()); // Kiểm tra có trả về JWT Token không
    }
}