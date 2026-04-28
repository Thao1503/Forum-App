package com.forumapp.config;

import com.cloudinary.Cloudinary;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class CloudinaryConfig {

    @Value("${cloudinary_name}")
    private String cloudinary_name;

    @Value("${cloudinary_api_key}")
    private String getCloudinary_api_key;

    @Value("${cloudinary_api_secret}")
    private String getCloudinary_api_secret;

    @Bean
    public Cloudinary cloudinary() {
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", cloudinary_name);
        config.put("api_key", getCloudinary_api_key);
        config.put("api_secret", getCloudinary_api_secret);
        return new Cloudinary(config);
    }
}
