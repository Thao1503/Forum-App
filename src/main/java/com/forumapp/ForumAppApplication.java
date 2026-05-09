package com.forumapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
@EnableJpaRepositories(basePackages = "com.forumapp.repository")
public class ForumAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(ForumAppApplication.class, args);
    }

}
