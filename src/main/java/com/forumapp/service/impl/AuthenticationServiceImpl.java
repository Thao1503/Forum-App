package com.forumapp.service.impl;


import com.forumapp.entity.UserEntity;
import com.forumapp.model.request.OtpRequest;
import com.forumapp.model.request.RegisterRequest;
import com.forumapp.repository.UserRepository;
import com.forumapp.service.AuthenticationService;
import com.forumapp.utils.EmailUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final EmailUtils emailUtils;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void register(RegisterRequest request){
        if(userRepository.existsByEmail(request.getEmail())){
            throw new RuntimeException("Email đã được đăng kí");
        }
        String otp = String.format("%06d", new Random().nextInt(1000000));
        redisTemplate.opsForValue().set("OTP : " + request.getEmail(), otp, 5, TimeUnit.MINUTES);
        redisTemplate.opsForValue().set("REG_DATA : " + request.getEmail(), request, 5, TimeUnit.MINUTES);
        emailUtils.sendOtpVerify(
                request.getEmail(),
                "Mã OTP của bạn",
                "Mã xác thực là : " + otp
        );
    }

    @Override
    public void sendOtp(OtpRequest request){
        String otp = String.format("%06d",new Random().nextInt(1000000));
        redisTemplate.opsForValue().set("OTP : " + request.getEmail(), otp, 5, TimeUnit.MINUTES);

        emailUtils.sendOtpVerify(
                request.getEmail(),
                "Mã OTP của bạn",
                "Mã xác thực là : " + otp
        );
    }


    @Override
    @Transactional
    public boolean verifyRegister(String email, String otpCode){
        String saveOtp = (String) redisTemplate.opsForValue().get("OTP : " + email);
        if(saveOtp != null && saveOtp.equals(otpCode)){
            RegisterRequest regData = (RegisterRequest) redisTemplate.opsForValue().get("REG_DATA : " + email);
            if(regData != null){
                UserEntity newUser = new UserEntity();
                newUser.setEmail(regData.getEmail());
                newUser.setUsername(regData.getUsername());
                String encodedPassword = passwordEncoder.encode(regData.getPassword());
                newUser.setPassword(encodedPassword);
                userRepository.save(newUser);
                redisTemplate.delete("OTP : " + email);
                redisTemplate.delete("RAG_DATA : " + email);
                return true;
            }
        }
        return false;
    }
}
