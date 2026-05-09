package com.forumapp.service;

import com.forumapp.model.request.ChangePasswordRequest;
import com.forumapp.model.request.PasswordRequest;
import com.forumapp.model.response.ProfileResponse;
import com.forumapp.model.response.SearchingResponse;
import com.forumapp.model.response.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserService {
    Page<UserResponse> getAllUsers(String search, String status, Pageable pageable);
    void editStatusAccount(Long id);
    void editMoreStatusAccount(List<Long> ids);
    ProfileResponse getProfileUser();

    Page<SearchingResponse> searchingHome(String search, Pageable pageable);

    void updateAvatar(String avatar);
    void changePassword(ChangePasswordRequest request);
//    String findOneUserName(String search);

}
