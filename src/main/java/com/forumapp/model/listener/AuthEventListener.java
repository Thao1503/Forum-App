package com.forumapp.model.listener;

import com.forumapp.entity.UserEntity;
import com.forumapp.entity.UserProfileEntity;
import com.forumapp.model.event.SendEmailEvent;
import com.forumapp.model.event.UserVerifiedEvent;
import com.forumapp.repository.UserProfileRepository;
import com.forumapp.repository.UserRepository;
import com.forumapp.utils.EmailUtils;
import lombok.AllArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class AuthEventListener {

    private final UserProfileRepository userProfileRepository;
    private final UserRepository userRepository;
    private final EmailUtils emailUtils;

    @EventListener
    public void onEvent(UserVerifiedEvent event) {
        UserEntity user = event.user();

        UserProfileEntity profile = UserProfileEntity.builder()
                .user(user)
                .points(0L)
                .build();

        userProfileRepository.save(profile);

        System.out.println("LOG EVENT: Đã khởi tạo thành công Profile cho User: " + user.getUsername());
    }

    @Async
    @EventListener
    public void onEvent(SendEmailEvent event){
            emailUtils.sendOtpVerify(
                    event.email(),
                    event.title(),
                    "Mã xác thực là : " + event.otp()
            );

    }
}
