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

        // FRONTEND URL
        String raw = System.getenv()
                .getOrDefault(
                        "APP_CORS_ALLOWED_ORIGINS",
                        "https://forum-front-end-pied.vercel.app"
                )
                .replace(" ", "");

        config.setAllowedOriginPatterns(
                Arrays.asList(raw.split(","))
        );

        config.setAllowedMethods(List.of(
                "GET",
                "POST",
                "PUT",
                "PATCH",
                "DELETE",
                "OPTIONS"
        ));

        config.setAllowedHeaders(List.of("*"));

        config.setExposedHeaders(List.of(
                "Authorization"
        ));

        // JWT Bearer token
        config.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", config);

        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable)

                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .authorizeHttpRequests(auth -> auth

                        // PRE-FLIGHT REQUEST
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // PUBLIC
                        .requestMatchers("/").permitAll()
                        .requestMatchers("/ws/**").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()

                        // PUBLIC GET APIs
                        .requestMatchers(HttpMethod.GET, "/api/**").permitAll()

                        // ADMIN
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // AUTHENTICATED APIs
                        .requestMatchers(HttpMethod.POST, "/api/category/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/post/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/comment/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/user/**").authenticated()

                        .requestMatchers(HttpMethod.PUT, "/api/user/**").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/post/**").authenticated()

                        .requestMatchers(HttpMethod.DELETE, "/api/comment/delete/**").authenticated()

                        .requestMatchers(HttpMethod.DELETE, "/api/post/delete/**").hasRole("ADMIN")

                        .anyRequest().authenticated()
                )

                .authenticationProvider(authenticationProvider)

                .addFilterBefore(
                        jwtAuthFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}