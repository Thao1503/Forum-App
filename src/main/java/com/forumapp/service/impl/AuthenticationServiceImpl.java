package com.forumapp.service.impl;


import com.forumapp.common.enums.UserStatus;
import com.forumapp.entity.RoleEntity;
import com.forumapp.entity.UserEntity;
import com.forumapp.entity.UserProfileEntity;
import com.forumapp.exception.DuplicateResourceException;
import com.forumapp.mapper.UserMapper;
import com.forumapp.model.event.SendEmailEvent;
import com.forumapp.model.event.UserVerifiedEvent;
import com.forumapp.model.request.LoginRequest;
import com.forumapp.model.request.OtpRequest;
import com.forumapp.model.request.PasswordRequest;
import com.forumapp.model.request.RegisterRequest;
import com.forumapp.model.response.LoginResponse;
import com.forumapp.model.response.UserResponse;
import com.forumapp.repository.RoleRepository;
import com.forumapp.repository.UserProfileRepository;
import com.forumapp.repository.UserRepository;
import com.forumapp.service.AuthenticationService;
import com.forumapp.utils.EmailUtils;
import com.forumapp.utils.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    private final ApplicationEventPublisher eventPublisher;


    @Value("${jwt.refresh-expiration}")
    private Long refreshExpiration;



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

        eventPublisher.publishEvent(new SendEmailEvent(request.getEmail(), otp, "Mã OTP xác thực tài khoản"));
    }

    @Override
    public void sendOtp(OtpRequest request){
        String otp = String.format("%06d",new Random().nextInt(1000000));
        redisTemplate.opsForValue().set("OTP:" + request.getEmail(), otp, 5, TimeUnit.MINUTES);

        eventPublisher.publishEvent(new SendEmailEvent(request.getEmail(), otp, "Mã OTP xác thực tài khoản"));
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

                newUser = userRepository.saveAndFlush(newUser);
                redisTemplate.delete(Arrays.asList("OTP:" + request.getEmail(), "REG_DATA:" + request.getEmail()));

                //Event listener
                eventPublisher.publishEvent(new UserVerifiedEvent(newUser));
        }
    }

    @Override
    public String forgotPassword(LoginRequest request) {
        UserEntity user = userRepository.findByUsernameOrEmail(request.getAccount(), request.getAccount())
                .orElseThrow(() -> new RuntimeException("Email hoặc tên người dùng không đúng"));


        String email = user.getEmail();
        redisTemplate.delete("OTP" + email);
        String otp = String.format("%06d", new Random().nextInt(1000000));
        redisTemplate.opsForValue().set("OTP:" + email, otp, 5, TimeUnit.MINUTES);

        eventPublisher.publishEvent(new SendEmailEvent(email, otp, "Mã OTP đặt lại mật khẩu tài khoản"));

        return email;
    }

    @Override
    public void verifyOtpForgotPassword(OtpRequest request){
        String saveOtp = (String) redisTemplate.opsForValue().get("OTP:" + request.getEmail());
        if(saveOtp == null || !saveOtp.equals(request.getOtp())) {
            throw new RuntimeException("Mã OTP không đúng hoặc đã hêt hạn");
        }
        redisTemplate.delete("OTP:" + request.getEmail());
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
    public LoginResponse login(LoginRequest request){
        UserEntity user = userRepository.findByUsernameOrEmail(request.getAccount(), request.getAccount())
                .orElseThrow(() -> new RuntimeException("Email hoặc tên người dùng không đúng"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Tên đăng nhập hoặc mật khẩu không đúng");
        }

        if (user.getStatus() ==  UserStatus.BANNED) {
            throw new RuntimeException("Tài khoản hiện đang bị khóa");
        }

        String accessToken = jwtUtils.generateAccessToken(user);
        String sessionId = UUID.randomUUID().toString();
        String refReshToken = UUID.randomUUID().toString();

        redisTemplate.opsForValue().set("REFRESH_TOKEN:" + refReshToken,sessionId,refreshExpiration, TimeUnit.MILLISECONDS);
        redisTemplate.opsForValue().set("REFRESH_SESSION:" + sessionId ,user.getEmail(),refreshExpiration, TimeUnit.MILLISECONDS);
        redisTemplate.opsForValue().set("REFRESH_SESSION_TOKEN:" + sessionId,refReshToken,refreshExpiration, TimeUnit.MILLISECONDS);

        return LoginResponse.builder()
                .email(user.getEmail())
                .accessToken(accessToken)
                .refreshToken(refReshToken)
                .build();
    }

    @Override
    public void logout(HttpServletRequest request, String refreshToken) {
        if (refreshToken != null && !refreshToken.isBlank()) {
            Object sessionIdObj = redisTemplate.opsForValue().get("REFRESH_TOKEN:" + refreshToken);
            String sessionId = sessionIdObj != null ? sessionIdObj.toString() : null;

            if (sessionId != null) {
                redisTemplate.delete("REFRESH_TOKEN:" + refreshToken);
                redisTemplate.delete("REFRESH_SESSION_TOKEN:" + sessionId);
                redisTemplate.delete("REFRESH_SESSION:" + sessionId);
            }
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String jwt = authHeader.substring(7);
            long timeOut = jwtUtils.getRemainingMillis(jwt);

            if(timeOut > 0){
                redisTemplate.opsForValue().set("BLACKLIST:" + jwt, "true", timeOut, TimeUnit.MILLISECONDS);
            }
        }
    }
    @Override
    public LoginResponse refreshAccessToken(String refreshToken){
        if(refreshToken == null || refreshToken.isBlank()){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Không tìm thấy Refresh Token");
        }
        Object sessionIdObj = redisTemplate.opsForValue().get("REFRESH_TOKEN:" + refreshToken);
        String sessionId = sessionIdObj != null ? sessionIdObj.toString() : null;
        if(sessionId == null){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token không hợp lệ hoặc hết hạn");
        }

        Object emailObj = redisTemplate.opsForValue().get("REFRESH_SESSION:" + sessionId);
        String email = emailObj != null ? emailObj.toString() : null;
        if (email == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Phiên đăng nhập không hợp lệ");
        }

        Object currentRtObj = redisTemplate.opsForValue().get("REFRESH_SESSION_TOKEN:" + sessionId);
        String currentRt = currentRtObj != null ? currentRtObj.toString() : null;
        if (currentRt == null || !currentRt.equals(refreshToken)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token không còn hoạt động");
        }

        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Không tìm thấy người dùng"));

        String newAccessToken = jwtUtils.generateAccessToken(user);

        String newRefreshToken = UUID.randomUUID().toString();

        redisTemplate.delete("REFRESH_TOKEN:" + refreshToken);

        redisTemplate.opsForValue().set("REFRESH_TOKEN:" + newRefreshToken, sessionId, refreshExpiration, TimeUnit.MILLISECONDS);
        redisTemplate.opsForValue().set("REFRESH_SESSION_TOKEN:" + sessionId, newRefreshToken, refreshExpiration, TimeUnit.MILLISECONDS);
        redisTemplate.expire("REFRESH_SESSION:" + sessionId, refreshExpiration, TimeUnit.MILLISECONDS);

        return LoginResponse.builder()
                .email(user.getEmail())
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .build();
    }







}
