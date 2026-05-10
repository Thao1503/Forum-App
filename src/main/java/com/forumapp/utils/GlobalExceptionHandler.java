package com.forumapp.utils;

import com.forumapp.exception.DuplicateResourceException;
import com.forumapp.model.response.ApiResponse;
import org.springframework.mail.MailException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage()));

        return ResponseEntity.badRequest().body(ApiResponse.<Map<String, String>>builder()
                .status(400)
                .message("Dữ liệu không hợp lệ")
                .data(errors)
                .build());
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicate(DuplicateResourceException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.<Void>builder()
                .status(409)
                .message(ex.getMessage())
                .build());
    }


    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiResponse<Void>> handleResponseStatusException(ResponseStatusException ex) {
        int code = ex.getStatusCode().value();
        return ResponseEntity.status(ex.getStatusCode()).body(ApiResponse.<Void>builder()
                .status(code)
                .message(ex.getReason() != null ? ex.getReason() : ex.getMessage())
                .build());
    }


    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Void>> handleRuntimeException(RuntimeException ex){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.<Void>builder()
                .status(400)
                .message(ex.getMessage())
                .build());
    }

    @ExceptionHandler(MailException.class)
    public ResponseEntity<ApiResponse<Void>> handleMailException(MailException ex) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(ApiResponse.<Void>builder()
                .status(503)
                .message("Khong gui duoc email OTP. Kiem tra SMTP/MAIL_PASSWORD tren server.")
                .build());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.<Void>builder()
                .status(500)
                .message("Đã xảy ra lỗi hệ thống")
                .build());
    }
}
