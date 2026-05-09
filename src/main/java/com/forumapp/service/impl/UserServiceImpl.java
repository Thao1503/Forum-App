package com.forumapp.service.impl;

import com.forumapp.common.enums.UserStatus;
import com.forumapp.entity.PostEntity;
import com.forumapp.entity.UserEntity;
import com.forumapp.entity.UserProfileEntity;
import com.forumapp.model.request.ChangePasswordRequest;
import com.forumapp.model.request.PasswordRequest;
import com.forumapp.model.response.PostResponse;
import com.forumapp.model.response.ProfileResponse;
import com.forumapp.model.response.SearchingResponse;
import com.forumapp.model.response.UserResponse;
import com.forumapp.repository.*;
import com.forumapp.security.UserPrincipal;
import com.forumapp.service.UserService;
import com.forumapp.utils.UserSpecifications;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.expression.spel.standard.SpelCompiler;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService{

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final PostRepository postRepository;
    private final CategoryRepository categoryRepository;
    private final CommentRepository commentRepository;

    @Autowired
    private CloudinaryService cloudinaryService;


    @Override
    public Page<UserResponse> getAllUsers(String search, String status, Pageable pageable){
        Specification<UserEntity> result = Specification
                .where(UserSpecifications.hasSearch(search))
                .and(UserSpecifications.hasStatus(status));

        return userRepository.findAll(result, pageable).map(this::convertToResponse);

    }

    private UserResponse convertToResponse(UserEntity user){
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .isVerified(user.isVerified())
                .status(user.getStatus().name())
                .roleName(user.getRoleEntity().getName())
                .createdAt(user.getCreatedAt())
                .avatar(user.getProfile() != null ? user.getProfile().getAvatar() : null)
                .build();
    }

    @Override
    public Page<SearchingResponse> searchingHome(String search, Pageable pageable){
        Specification<PostEntity> list = Specification.where(UserSpecifications.hasSearchingByTitleOrContent(search));

        return postRepository.findAll(list, pageable).map(post -> SearchingResponse.builder()
                .username(post.getAuthor().getUsername())
                .avatar(post.getAuthor().getProfile().getAvatar())
                .title(post.getTitle())
                .content(post.getContent())
                .slug(post.getSlug())
                .category(categoryRepository.findByPosts_Slug(post.getSlug()).getName())
                .categorySlug(categoryRepository.findByPosts_Slug(post.getSlug()).getSlug())
                .date(post.getCreatedAt())
                .build());
    }


    @Override
    @Transactional
    public void editStatusAccount(Long id){
        UserEntity user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("Nguoi dung khong ton tai"));
        if(user.getRoleEntity().getName().equals("ADMIN")){
            throw new RuntimeException("Admin");
        }
        if (user.getStatus() == UserStatus.ACTIVE) {
            user.setStatus(UserStatus.BANNED);
        } else if (user.getStatus() == UserStatus.BANNED) {
            user.setStatus(UserStatus.ACTIVE);
        }

        userRepository.save(user);
    }

    @Override
    @Transactional
    public void editMoreStatusAccount(List<Long> ids) {
        List<UserEntity> users = userRepository.findAllById(ids);

        for (UserEntity user : users) {
            user.setStatus(user.getStatus() == UserStatus.ACTIVE ? UserStatus.BANNED : UserStatus.ACTIVE);
        }
        userRepository.saveAll(users);
    }

    @Override
    public ProfileResponse getProfileUser(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof UserPrincipal up)) {
            throw new RuntimeException("Người dùng chưa đăng nhập");
        }
        UserEntity user = userRepository.findById(up.getId()).orElseThrow(() -> new RuntimeException("Người dùng không tồn tại "));
        UserProfileEntity profile = userProfileRepository.findByUserId(user.getId());
        return ProfileResponse.builder()
                .name(profile.getUser().getUsername())
                .email(profile.getUser().getEmail())
                .point(profile.getPoints())
                .rank(profile.getRank())
                .avatar(profile.getAvatar())
                .phone(profile.getPhone())
                .dob(profile.getDob())
                .build();
    }

    @Override
    public void updateAvatar(String avatar){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof UserPrincipal up)) {
            throw new RuntimeException("Người dùng chưa đăng nhập");
        }
        UserEntity user = userRepository.findById(up.getId()).orElseThrow(() -> new RuntimeException("Người dùng không tồn tại "));
        UserProfileEntity profile = userProfileRepository.findByUserId(user.getId());
        profile.setAvatar(normalizeAvatar(avatar));
        userProfileRepository.save(profile);
    }

    @Override
    public void changePassword(ChangePasswordRequest request){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof UserPrincipal up)) {
            throw new RuntimeException("Người dùng chưa đăng nhập");
        }
        UserEntity user = userRepository.findById(up.getId()).orElseThrow(() -> new RuntimeException("Người dùng không tồn tại "));
        if(!passwordEncoder.matches(request.getOldPassword(), user.getPassword())){
            throw new RuntimeException("Mật khẩu cũ không đúng");
        }
        if(!request.getPassword().equals(request.getPasswordConfirm())){
            throw new RuntimeException("Mật khẩu không khớp nhau");
        }
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);
    }

    private String normalizeAvatar(String raw) {
        if (raw == null) return null;
        String s = raw.trim();
        if ((s.startsWith("\"") && s.endsWith("\"")) || (s.startsWith("'") && s.endsWith("'"))) {
            s = s.substring(1, s.length() - 1).trim();
        }
        if (s.startsWith("http://")) s = "https://" + s.substring(7);
        return s;
    }
}
