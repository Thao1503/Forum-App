package com.forumapp.api.post;

import com.forumapp.entity.UserEntity;
import com.forumapp.model.response.UserResponse;
import com.forumapp.service.AuthenticationService;
import lombok.AllArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping(value = "/api/user")
public class UserController {

    private final AuthenticationService authenticationService;

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/all")
    public ResponseEntity<List<UserResponse>> getAllUser(){
        authenticationService.getAllUsers();
        return ResponseEntity.ok(authenticationService.getAllUsers());
    }
}
