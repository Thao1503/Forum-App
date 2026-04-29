package com.forumapp.service;

import com.forumapp.model.request.PasswordRequest;
import com.forumapp.model.request.LoginRequest;
import com.forumapp.model.request.OtpRequest;
import com.forumapp.model.request.RegisterRequest;
import com.forumapp.model.response.LoginResponse;
import com.forumapp.model.response.UserResponse;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface AuthenticationService {

    void register(RegisterRequest request);
    void sendOtp(OtpRequest request);
    void verifyRegister(OtpRequest request);
    void forgotPassword(LoginRequest request);
    void verifyOtpForgotPassword(OtpRequest request);
    void resetPassword(PasswordRequest request);
    LoginResponse login(LoginRequest request);
    void logout(HttpServletRequest request, String refreshToken);
    LoginResponse refreshAccessToken(String refreshToken);

    List<UserResponse> getAllUsers();

}
