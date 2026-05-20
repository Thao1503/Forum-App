package com.forumapp.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        String raw = System.getenv().getOrDefault(
                "APP_CORS_ALLOWED_ORIGINS",
                "http://localhost:3000,http://localhost:5173,https://forum-front-end-pied.vercel.app"
        ).replace(" ", "");

        // Nếu là production và muốn an toàn tuyệt đối, dùng danh sách cụ thể thay vì "*"
        if (raw.equals("*")) {
            config.addAllowedOriginPattern("*");
        } else {
            config.setAllowedOrigins(Arrays.asList(raw.split(",")));
        }

        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        // Bổ sung đầy đủ các Headers để không bị chặn Pre-flight request
        config.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "Accept",
                "Origin",
                "X-Requested-With",
                "Cache-Control"
        ));

        config.setExposedHeaders(List.of("Authorization"));

        // Cho phép gửi credentials (token/cookies) - cực kỳ quan trọng cho frontend hiện đại
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                // Kích hoạt cấu hình CORS đã định nghĩa ở trên
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                .authorizeHttpRequests(auth -> auth
                        // Ưu tiên cho phép các yêu cầu OPTIONS (CORS Pre-flight) đi qua
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        .requestMatchers("/ws/**").permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/auth/**").permitAll()

                        // PUBLIC GET APIs
                        .requestMatchers(HttpMethod.GET, "/api/category/**", "/api/post/**", "/api/comment/**", "/api/utils/**", "/api/user/**").permitAll()

                        // AUTHENTICATED APIs
                        .requestMatchers(HttpMethod.POST, "/api/category/**", "/api/post/**", "/api/comment/**", "/api/user/**").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/user/**", "/api/post/**").authenticated()

                        // ROLES
                        .requestMatchers(HttpMethod.DELETE, "/api/post/delete/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/comment/delete/**").authenticated()

                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}