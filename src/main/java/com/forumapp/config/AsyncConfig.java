//package com.forumapp.config;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.scheduling.annotation.EnableAsync;
//import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
//
//import java.util.concurrent.Executor;
//
//@Configuration
//@EnableAsync
//public class AsyncConfig {
//
//    @Bean(name = "taskExecutor") // Đặt đúng tên này để Spring tự bắt lấy làm mặc định
//    public Executor taskExecutor() {
//        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
//        executor.setCorePoolSize(5);      // Số luồng tối thiểu chạy thường trực
//        executor.setMaxPoolSize(10);     // Số luồng tối đa khi quá tải
//        executor.setQueueCapacity(500);  // Hàng đợi cho tác vụ chờ
//        executor.setThreadNamePrefix("ForumAsync-");
//        executor.initialize();
//        return executor;
//    }
//}
