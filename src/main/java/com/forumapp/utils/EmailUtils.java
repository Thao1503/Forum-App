package com.forumapp.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;

@Component
@RequiredArgsConstructor
public class EmailUtils {

    @Value("${SENDGRID_API_KEY:}")
    private String sendGridApiKey;

    @Value("${MAIL_FROM:}")
    private String mailFrom;

    public void sendOtpVerify(String to, String subject, String content) {
        if (sendGridApiKey == null || sendGridApiKey.isBlank()) {
            throw new IllegalStateException("Missing SENDGRID_API_KEY env var");
        }
        if (mailFrom == null || mailFrom.isBlank()) {
            throw new IllegalStateException("Missing MAIL_FROM env var (must be a verified sender in SendGrid)");
        }

        try {
            Email from = new Email(mailFrom);
            Email toEmail = new Email(to);
            Content body = new Content("text/plain", content);
            Mail mail = new Mail(from, subject, toEmail, body);

            SendGrid sg = new SendGrid(sendGridApiKey);
            Request request = new Request();
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sg.api(request);
            if (response.getStatusCode() >= 400) {
                throw new RuntimeException(
                        "SendGrid send failed: " + response.getStatusCode() + " " + response.getBody()
                );
            }
        } catch (Exception e) {
            throw new RuntimeException("SendGrid send failed", e);
        }
    }
}
