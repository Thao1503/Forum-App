package com.forumapp.service;

import com.forumapp.entity.UserEntity;
import com.forumapp.model.request.OtpRequest;
import com.forumapp.model.request.RegisterRequest;

import java.util.List;

public interface AuthenticationService {

    void register(RegisterRequest request);
    void sendOtp(OtpRequest request);
    boolean verifyRegister(String email, String otpCode);
}
