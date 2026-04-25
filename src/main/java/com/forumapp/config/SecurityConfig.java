package com.forumapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. Tắt CSRF vì mình dùng API (Stateless)
                .csrf(AbstractHttpConfigurer::disable)

                // 2. Cấu hình phân quyền
                .authorizeHttpRequests(auth -> auth
                        // Cho phép tất cả mọi người truy cập các API bắt đầu bằng /api/auth/ (như register, login)
                        .requestMatchers("/api/auth/**").permitAll()
                        // Tất cả các API khác mới cần phải đăng nhập
                        .anyRequest().authenticated()
                );

        return http.build();
    }
}
