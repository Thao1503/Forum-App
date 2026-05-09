package com.forumapp.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class SlugUtils {

    public String formatSlug(String input){
        if (input == null || input.isBlank()) {
            return "";
        }
        String normalized = input.toLowerCase();

        normalized = Normalizer.normalize(normalized, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        normalized = pattern.matcher(normalized).replaceAll("");

        normalized = normalized.replace('đ', 'd').replace('Đ', 'd');

        normalized = normalized.replaceAll("[^a-z0-9\\s]", "");

        normalized = normalized.trim().replaceAll("\\s+", "-");

        return normalized;
    }
}
