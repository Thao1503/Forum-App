package com.forumapp.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailUtils {

    private final JavaMailSender mailSender;
    private final ResendEmailClient resendEmailClient;

    @Async
    public void sendOtpVerify(String to, String subject, String content){
        // Prefer Resend (HTTP) in hosted environments where outbound SMTP is blocked.
        try {
            resendEmailClient.sendTextEmail(to, subject, content);
            return;
        } catch (Exception resendErr) {
            log.warn("Resend failed, falling back to SMTP (may be blocked): {}", resendErr.getMessage());
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(content);
        mailSender.send(message);
    }
}
