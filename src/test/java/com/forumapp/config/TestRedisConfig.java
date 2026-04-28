package com.forumapp;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.boot.test.context.TestConfiguration;

@TestConfiguration
public class TestRedisConfig {
    private final redis.embedded.RedisServer redisServer;

    public TestRedisConfig() {
        this.redisServer = new redis.embedded.RedisServer(6379);
    }

    @PostConstruct
    public void postConstruct() {
        redisServer.start();
    }

    @PreDestroy
    public void preDestroy() {
        redisServer.stop();
    }
}
