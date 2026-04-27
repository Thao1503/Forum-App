package com.forumapp.service;

import com.forumapp.model.request.OtpRequest;
import com.forumapp.model.request.RegisterRequest;
import com.forumapp.model.response.LoginResponse;

public interface AuthenticationService {

    void register(RegisterRequest request);
//    void forgetPassword(UserEntity userEntity);
    void sendOtp(String email);
    void verifyRegister(String email, String otpCode);
    String forgotPassword(String identifier);
    void verifyOtpForgotPassword(String email, String otpCode);
    void resetPassword(String email, String password, String passwordConfirm);
    String login(String identifier, String password);
}
