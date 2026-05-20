package com.forumapp.model.event;

public record SendEmailEvent(String email, String otp, String title) {
}
