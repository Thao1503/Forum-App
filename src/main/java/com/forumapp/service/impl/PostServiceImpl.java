package com.forumapp.service.impl;

import com.forumapp.config.JwtAuthenticationFilter;
import com.forumapp.entity.*;
import com.forumapp.mapper.PostMapper;
import com.forumapp.model.repository.PostStats;
import com.forumapp.model.request.PostRequest;
import com.forumapp.model.response.CommentResponse;
import com.forumapp.model.response.FollowResponse;
import com.forumapp.model.response.PostResponse;
import com.forumapp.model.response.StatisticResponse;
import com.forumapp.repository.*;
import com.forumapp.security.UserPrincipal;
import com.forumapp.service.NotificationService;
import com.forumapp.service.PostService;
import com.forumapp.utils.JwtUtils;
import com.forumapp.utils.SlugUtils;
import io.lettuce.core.dynamic.annotation.Param;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.http.HttpRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final PostMapper postMapper;
    private final SlugUtils slugUtils;
    private final CategoryRepository categoryRepository;
    private final CommentRepository commentRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final FollowRepository followRepository;
    private final JwtUtils jwtUtils;
    private final LikeRepository likeRepository;
    private final NotificationService notificationService;
    private final UserProfileRepository userProfileRepository;


    @Override
    public StatisticResponse getStatisticForum(){
        PostStats stat1 = postRepository.getPostStatistics();
        PostStats stat2 = userRepository.getAllUser();

        return StatisticResponse.builder()
                .totalThread(stat1.getTotalThreads())
                .totalReply(stat1.getTotalReplies() != null ? stat1.getTotalReplies() : 0)
                .totalMember(stat2.getTotalMembers())
                .build();
    }

    @Override
    public List<PostResponse> findTop5PostNewest(){
        return postRepository.findTop5ByOrderByCreatedAtDesc(PageRequest.of(0,5)).stream()
                .map(post -> PostResponse.builder()
                        .id(post.getId())
                        .title(post.getTitle())
                        .username(post.getAuthor().getUsername())
                        .avatar(post.getAuthor().getProfile().getAvatar())
                        .slug(post.getSlug())
                        .createdAt(post.getCreatedAt())
                        .build()).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PostResponse createPost(String slug, PostRequest request){
        CategoryEntity category = categoryRepository.findBySlug(slug);
        UserPrincipal up = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserEntity user = userRepository.findById(up.getId()).orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));

        PostEntity post = postMapper.toEntity(request);
        post.setAuthor(user);
        post.setSlug(slugUtils.formatSlug(post.getTitle()));
        post.setCategory(category);
        postRepository.save(post);

        post.setSlug(post.getSlug() + "." + post.getId());
        Long count =  category.getThreadCount() + 1;
        category.setThreadCount(count);
        postRepository.save(post);

        UserProfileEntity profile = userProfileRepository.findByUserId(up.getId());
        Long point = profile.getPoints() + 5L;
        profile.setPoints(point);
        userProfileRepository.save(profile);

        FollowPostEntity follow = FollowPostEntity.builder()
                .post(post)
                .user(user)
                .checked(Boolean.TRUE)
                .build();

        followRepository.save(follow);

        String viewCheckToken = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set("VIEW_TOKEN:" + user.getEmail(), viewCheckToken,24, TimeUnit.HOURS);

        return PostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .username(post.getAuthor().getUsername())
                .slug(post.getSlug())
                .createdAt(post.getCreatedAt())
                .totalReplies(post.getReplies())
                .totalViews(post.getViews())
                .build();
    }

    @Override
    public PostResponse getDetailPost(String slug, Pageable pageable, HttpServletRequest request, HttpServletResponse response) {
        PostEntity post = postRepository.findBySlug(slug);
        if (post == null) {
            throw new RuntimeException("Bài viết không tồn tại");
        }

        if(post.getHide() == Boolean.TRUE){
            throw new RuntimeException("Bài viết đã bị xóa");
        }

        Pageable likePageable = PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "id"));



        Page<CommentEntity> commentList = commentRepository.findByPostId(post.getId(), pageable);
        Page<CommentResponse> list = commentList.map(cmt -> CommentResponse.builder()
                .id(cmt.getId())
                .avatar(cmt.getUser().getProfile().getAvatar())
                .username(cmt.getUser().getUsername())
                .content(cmt.getContent())
                .slug(cmt.getSlug())
                .quoted(cmt.getQuoted())
                .quotedPost(cmt.getQuotedPost())
                .likes(notificationService.getAllLikeComment(cmt.getId(), likePageable))
                .createAt(cmt.getCreatedAt())
                .updateAt(cmt.getUpdatedAt())
                .build());


        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserEntity user = null;

        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof UserPrincipal up) {
            user = userRepository.findById(up.getId())
                    .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));
        }

        String viewKey;
        List<FollowResponse> result = new ArrayList<>();
        if (user != null) {
            viewKey = "VIEW_TOKEN:USER:" + user.getId() + ":POST:" + post.getId();
            List<FollowPostEntity> follow = followRepository.findByPost_IdAndUser_Id(post.getId(), user.getId());
            result = follow.stream().map(fl -> FollowResponse.builder()
                    .checked(fl.getChecked())
                    .followId(fl.getId())
                    .build()).collect(Collectors.toList());
        } else {
            String guestId = jwtUtils.getOrCreateGuestId(request, response);
            viewKey = "VIEW_TOKEN:GUEST:" + guestId + ":POST:" + post.getId();
        }

        String checkTokenView = (String) redisTemplate.opsForValue().get(viewKey);
        if (checkTokenView == null) {
            post.setViews(post.getViews() + 1L);
            postRepository.save(post);
            redisTemplate.opsForValue().set(viewKey, UUID.randomUUID().toString(), 24, TimeUnit.HOURS);
        }


        return PostResponse.builder()
                .id(post.getId())
                .username(post.getAuthor().getUsername())
                .avatar(post.getAuthor().getProfile().getAvatar())
                .rank(post.getAuthor().getProfile().getRank())
                .title(post.getTitle())
                .content(post.getContent())
                .slug(post.getSlug())
                .followPost(result)
                .likes(notificationService.getAllLikePost(post.getId(), likePageable))
                .comments(list)
                .locked(post.getLocked())
                .totalReplies(post.getReplies())
                .totalViews(post.getViews())
                .createdAt(post.getCreatedAt())
                .build();
    }

    @Override
    public PostResponse editPost(Long id, PostRequest request, Pageable pageable){
        PostEntity post = postRepository.findById(id).orElseThrow(() -> new RuntimeException("Bài đăng không tồn tại"));
        post.setContent(request.getContent());
        postRepository.save(post);
        Page<CommentEntity> commentList = commentRepository.findByPostId(post.getId(), pageable);
        Page<CommentResponse> list = commentList.map(cmt -> CommentResponse.builder()
                .id(cmt.getId())
                .avatar(cmt.getUser().getProfile().getAvatar())
                .username(cmt.getUser().getUsername())
                .content(cmt.getContent())
                .slug(cmt.getSlug())
                .createAt(cmt.getCreatedAt())
                .updateAt(cmt.getUpdatedAt())
                .build());

        List<FollowPostEntity> arr1 = followRepository.findByPostId(id);
        List<FollowResponse> arr2 = arr1.stream().map(fl -> FollowResponse.builder()
                .checked(fl.getChecked())
                .followId(fl.getId())
                .build()).collect(Collectors.toList());
        return PostResponse.builder()
                .id(post.getId())
                .username(post.getAuthor().getUsername())
                .avatar(post.getAuthor().getProfile().getAvatar())
                .title(post.getTitle())
                .content(post.getContent())
                .slug(post.getSlug())
                .comments(list)
                .followPost(arr2)
                .totalReplies(post.getReplies())
                .totalViews(post.getViews())
                .createdAt(post.getCreatedAt())
                .build();
    }

    @Override
    @Transactional
    public void deletePost(Long id){
        PostEntity post = postRepository.findById(id).orElseThrow(() -> new RuntimeException("Bài đăng không tồn tại"));
        post.setHide(true);
        postRepository.save(post);
    }

    @Override
    public Page<PostResponse> getAllPostFollow(@Param("userId") Pageable pageable){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof UserPrincipal up)) {
            throw new RuntimeException("Người dùng chưa đăng nhập");
        }
        UserEntity user = userRepository.findById(up.getId()).orElseThrow(() -> new RuntimeException("Người dùng không tồn tại "));


        Page<PostEntity> list = postRepository.findFollowedPosts(user.getId(),pageable);
        Page<PostResponse> result = list.map(post -> PostResponse.builder()
                        .id(post.getId())
                        .title(post.getTitle())
                        .username(post.getAuthor().getUsername())
                        .avatar(post.getAuthor().getProfile().getAvatar())
                        .slug(post.getSlug())
                        .createdAt(post.getCreatedAt())
                        .build());

        return result;
    }

    @Override
    public Page<PostResponse> findByUserId(Pageable pageable){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof UserPrincipal up)) {
            throw new RuntimeException("Người dùng chưa đăng nhập");
        }
        UserEntity user = userRepository.findById(up.getId()).orElseThrow(() -> new RuntimeException("Người dùng không tồn tại "));
        Page<PostEntity> list = postRepository.findByAuthor_Id(user.getId(), pageable);
        Page<PostResponse> result = list.map(post -> PostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .username(post.getAuthor().getUsername())
                .avatar(post.getAuthor().getProfile().getAvatar())
                .slug(post.getSlug())
                .createdAt(post.getCreatedAt())
                .build());
        return result;
    }

    @Override
    public void lockedPost(Long id){
        PostEntity post = postRepository.findById(id).orElseThrow(() -> new RuntimeException("Bài đăng không tồn tại"));
        post.setLocked(true);
        postRepository.save(post);
    }

    @Override
    public List<PostResponse> findTop5PostByAdmin(){
        return postRepository.findPostsByRoleAndGroup("ADMIN", "GENERAL", PageRequest.of(0, 5)).stream()
                .map(post -> PostResponse.builder()
                        .id(post.getId())
                        .title(post.getTitle())
                        .username(post.getAuthor().getUsername())
                        .avatar(post.getAuthor().getProfile().getAvatar())
                        .slug(post.getSlug())
                        .createdAt(post.getCreatedAt())
                        .build()).collect(Collectors.toList());
    }




}
