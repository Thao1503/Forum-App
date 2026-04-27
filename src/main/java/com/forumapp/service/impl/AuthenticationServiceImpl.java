package com.forumapp.service.impl;


import com.forumapp.common.enums.UserStatus;
import com.forumapp.entity.RoleEntity;
import com.forumapp.entity.UserEntity;
import com.forumapp.exception.DuplicateResourceException;
import com.forumapp.mapper.UserMapper;
import com.forumapp.model.request.RegisterRequest;
import com.forumapp.model.response.LoginResponse;
import com.forumapp.repository.RoleRepository;
import com.forumapp.repository.UserRepository;
import com.forumapp.service.AuthenticationService;
import com.forumapp.utils.EmailUtils;
import com.forumapp.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final EmailUtils emailUtils;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final RoleRepository roleRepository;
    private final JwtUtils jwtUtils;

    @Override
    public void register(RegisterRequest request){
        if(userRepository.existsByEmail(request.getEmail())){
            throw new DuplicateResourceException("Email đã tồn tại");
        }

        if(userRepository.existsByUsername(request.getUsername())){
            throw new DuplicateResourceException("Tên người dùng đã tồn tại.");
        }
        String otp = String.format("%06d", new Random().nextInt(1000000));
        redisTemplate.opsForValue().set("OTP:" + request.getEmail(), otp, 5, TimeUnit.MINUTES);
        redisTemplate.opsForValue().set("REG_DATA:" + request.getEmail(), request, 5, TimeUnit.MINUTES);
        emailUtils.sendOtpVerify(
                request.getEmail(),
                "Mã OTP của bạn",
                "Mã xác thực là : " + otp
        );
    }

    @Override
    public void sendOtp(String email){
        String otp = String.format("%06d",new Random().nextInt(1000000));
        redisTemplate.opsForValue().set("OTP:" + email, otp, 5, TimeUnit.MINUTES);

        emailUtils.sendOtpVerify(
                email,
                "Mã OTP của bạn",
                "Mã xác thực là : " + otp
        );
    }


    @Override
    @Transactional
    public void verifyRegister(String email, String otpCode){
        String saveOtp = (String) redisTemplate.opsForValue().get("OTP:" + email);
        if(saveOtp == null || !saveOtp.equals(otpCode)){
            throw new RuntimeException("Mã OTP không đúng hoặc đã hêt hạn");
        }
            RegisterRequest regData = (RegisterRequest) redisTemplate.opsForValue().get("REG_DATA:" + email);
            if(regData != null){
                UserEntity newUser = userMapper.toEntity(regData);
                RoleEntity memberRole = roleRepository.findByName("MEMBER")
                        .orElseThrow(() -> new RuntimeException("Error: Role MEMBER is not found."));
                newUser.setRoleEntity(memberRole);
                newUser.setPassword(passwordEncoder.encode(regData.getPassword()));
                newUser.setVerified(true);
                newUser.setStatus(UserStatus.ACTIVE);
                userRepository.save(newUser);
                redisTemplate.delete(Arrays.asList("OTP:" + email, "REG_DATA:" + email));
        }
    }

    @Override
    public String forgotPassword(String identifier) {
        UserEntity user = userRepository.findByUsernameOrEmail(identifier, identifier)
                .orElseThrow(() -> new RuntimeException("Email hoặc tên người dùng không đúng"));

        String email = user.getEmail();
        String otp = String.format("%06d", new Random().nextInt(1000000));
        redisTemplate.opsForValue().set("OTP:" + email, otp, 5, TimeUnit.MINUTES);
        emailUtils.sendOtpVerify(
                email,
                "Mã OTP phục hồi mật khẩu",
                "Mã xác thực của bạn là: " + otp
        );
        return user.getEmail();
    }

    @Override
    public void verifyOtpForgotPassword(String email, String otpCode){
        String saveOtp = (String) redisTemplate.opsForValue().get("OTP:" + email);
        if(saveOtp == null || !saveOtp.equals(otpCode)) {
            throw new RuntimeException("Mã OTP không đúng hoặc đã hêt hạn");
        }
        redisTemplate.opsForValue().set("RESET_AUTH:" + email, "verified", 5, TimeUnit.MINUTES);
    }

    @Override
    @Transactional
    public void resetPassword(String email,String password, String passwordConfirm){
        if(!password.equals(passwordConfirm)){
            throw new RuntimeException("Mật khẩu không khớp nhau");
        }
        String check = (String) redisTemplate.opsForValue().get("RESET_AUTH:" + email);
        if(check == null){
            throw new RuntimeException("Yêu cầu không hợp lệ hoặc phiên làm việc đã hết hạn");
        }
        UserEntity user = userRepository.findByEmail(email)
                        .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);
        redisTemplate.delete("REST_AUTH:" + email);
    }

    @Override
    public String login(String identifier, String password){
        UserEntity user = userRepository.findByUsernameOrEmail(identifier, identifier)
                .orElseThrow(() -> new RuntimeException("Email hoặc tên người dùng không đúng"));
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Tên đăng nhập hoặc mật khẩu không đúng");
        }

        if (!user.isVerified()) {
            throw new RuntimeException("Tài khoản chưa được kích hoạt qua Email");
        }

        if (user.getStatus().equals("BANNED")) {
            throw new RuntimeException("Tài khoản hiện đang bị khóa");
        }

        return jwtUtils.generateToken(user);
    }



}
