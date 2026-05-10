package com.forumapp.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailUtils {

    private final JavaMailSender mailSender;

    public void sendOtpVerify(String to, String subject, String content){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(content);
        try {
            mailSender.send(message);
        } catch (MailException e) {
            // Important: don't pretend the OTP was sent; surface this in logs and the API response.
            log.error("Failed to send OTP email to {}", to, e);
            throw e;
        }
    }
}
