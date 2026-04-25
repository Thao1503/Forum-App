package com.forumapp.service;

import com.forumapp.model.request.OtpRequest;
import com.forumapp.model.request.RegisterRequest;

public interface AuthenticationService {

    void register(RegisterRequest request);
    void sendOtp(OtpRequest request);
    boolean verifyRegister(String email, String otpCode);
}
