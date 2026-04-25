package com.forumapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class ForumAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(ForumAppApplication.class, args);
    }

}
