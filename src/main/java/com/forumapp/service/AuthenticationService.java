package com.forumapp.service;

import com.forumapp.model.request.PasswordRequest;
import com.forumapp.model.request.LoginRequest;
import com.forumapp.model.request.OtpRequest;
import com.forumapp.model.request.RegisterRequest;

public interface AuthenticationService {

    void register(RegisterRequest request);
    void sendOtp(OtpRequest request);
    void verifyRegister(OtpRequest request);
    void forgotPassword(LoginRequest request);
    void verifyOtpForgotPassword(OtpRequest request);
    void resetPassword(PasswordRequest request);
    String login(LoginRequest request);
}
