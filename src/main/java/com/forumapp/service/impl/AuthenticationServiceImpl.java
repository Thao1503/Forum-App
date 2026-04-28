package com.forumapp.service.impl;


import com.forumapp.common.enums.UserStatus;
import com.forumapp.entity.RoleEntity;
import com.forumapp.entity.UserEntity;
import com.forumapp.entity.UserProfileEntity;
import com.forumapp.exception.DuplicateResourceException;
import com.forumapp.mapper.UserMapper;
import com.forumapp.model.request.LoginRequest;
import com.forumapp.model.request.OtpRequest;
import com.forumapp.model.request.PasswordRequest;
import com.forumapp.model.request.RegisterRequest;
import com.forumapp.model.response.LoginResponse;
import com.forumapp.repository.RoleRepository;
import com.forumapp.repository.UserProfileRepository;
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
    private final UserProfileRepository profileRepository;
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
    public void sendOtp(OtpRequest request){
        String otp = String.format("%06d",new Random().nextInt(1000000));
        redisTemplate.opsForValue().set("OTP:" + request.getEmail(), otp, 5, TimeUnit.MINUTES);

        emailUtils.sendOtpVerify(
                request.getEmail(),
                "Mã OTP của bạn",
                "Mã xác thực là : " + otp
        );
    }


    @Override
    @Transactional
    public void verifyRegister(OtpRequest request){
        String saveOtp = (String) redisTemplate.opsForValue().get("OTP:" + request.getEmail());
        if(saveOtp == null || !saveOtp.equals(request.getOtp())){
            throw new RuntimeException("Mã OTP không đúng hoặc đã hêt hạn");
        }
            RegisterRequest regData = (RegisterRequest) redisTemplate.opsForValue().get("REG_DATA:" + request.getEmail());
            if(regData != null){
                UserEntity newUser = userMapper.toEntity(regData);
                RoleEntity memberRole = roleRepository.findByName("MEMBER")
                        .orElseThrow(() -> new RuntimeException("Error: Role MEMBER is not found."));
                newUser.setRoleEntity(memberRole);
                newUser.setPassword(passwordEncoder.encode(regData.getPassword()));
                newUser.setVerified(true);
                newUser.setStatus(UserStatus.ACTIVE);

                UserProfileEntity profile = UserProfileEntity.builder()
                                .user(newUser)
                                .build();

                profile.setUser(newUser);

                newUser.setProfile(profile);
                userRepository.save(newUser);
                redisTemplate.delete(Arrays.asList("OTP:" + request.getEmail(), "REG_DATA:" + request.getEmail()));
        }
    }

    @Override
    public void forgotPassword(LoginRequest request) {
        UserEntity user = userRepository.findByUsernameOrEmail(request.getAccount(), request.getAccount())
                .orElseThrow(() -> new RuntimeException("Email hoặc tên người dùng không đúng"));

        String email = user.getEmail();
        String otp = String.format("%06d", new Random().nextInt(1000000));
        redisTemplate.opsForValue().set("OTP:" + email, otp, 5, TimeUnit.MINUTES);
        emailUtils.sendOtpVerify(
                email,
                "Mã OTP phục hồi mật khẩu",
                "Mã xác thực của bạn là: " + otp
        );
    }

    @Override
    public void verifyOtpForgotPassword(OtpRequest request){
        String saveOtp = (String) redisTemplate.opsForValue().get("OTP:" + request.getEmail());
        if(saveOtp == null || !saveOtp.equals(request.getOtp())) {
            throw new RuntimeException("Mã OTP không đúng hoặc đã hêt hạn");
        }
        redisTemplate.opsForValue().set("RESET_AUTH:" + request.getEmail(), "verified", 5, TimeUnit.MINUTES);
    }

    @Override
    @Transactional
    public void resetPassword(PasswordRequest request){
        if(!request.getPassword().equals(request.getPasswordConfirm())){
            throw new RuntimeException("Mật khẩu không khớp nhau");
        }
        String check = (String) redisTemplate.opsForValue().get("RESET_AUTH:" + request.getEmail());
        if(check == null){
            throw new RuntimeException("Yêu cầu không hợp lệ hoặc phiên làm việc đã hết hạn");
        }
        UserEntity user = userRepository.findByEmail(request.getEmail())
                        .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);
        redisTemplate.delete("RESET_AUTH:" + request.getEmail());
    }

    @Override
    public String login(LoginRequest request){
        UserEntity user = userRepository.findByUsernameOrEmail(request.getAccount(), request.getAccount())
                .orElseThrow(() -> new RuntimeException("Email hoặc tên người dùng không đúng"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Tên đăng nhập hoặc mật khẩu không đúng");
        }

        if (user.getStatus() ==  UserStatus.BANNED) {
            throw new RuntimeException("Tài khoản hiện đang bị khóa");
        }

        String token = jwtUtils.generateToken(user);

        redisTemplate.opsForValue().set("TOKEN:" + user.getEmail(), token, 24, TimeUnit.HOURS);

        return token;
    }



}
