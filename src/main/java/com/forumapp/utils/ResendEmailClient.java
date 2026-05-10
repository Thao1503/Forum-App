package com.forumapp.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class ResendEmailClient {

    private final RestClient restClient = RestClient.builder()
            .baseUrl("https://api.resend.com")
            .build();

    @Value("${resend.api-key:}")
    private String apiKey;

    @Value("${resend.from:}")
    private String from;

    public void sendTextEmail(String to, String subject, String text) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("Missing resend.api-key");
        }
        if (from == null || from.isBlank()) {
            throw new IllegalStateException("Missing resend.from");
        }

        try {
            restClient.post()
                    .uri("/emails")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .body(Map.of(
                            "from", from,
                            "to", new String[]{to},
                            "subject", subject,
                            "text", text
                    ))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException e) {
            // Resend returns JSON details; status+body are useful in logs.
            log.error("Resend send failed: status={} body={}", e.getStatusCode(), e.getResponseBodyAsString(), e);
            throw e;
        }
    }
}

