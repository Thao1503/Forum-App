package com.forumapp.service.impl;

import com.forumapp.entity.*;
import com.forumapp.model.event.LikeEvent;
import com.forumapp.model.request.NotificationRequest;
import com.forumapp.model.response.LikeResponse;
import com.forumapp.model.response.NotificationResponse;
import com.forumapp.model.response.NotificationWsDto;
import com.forumapp.repository.*;
import com.forumapp.security.UserPrincipal;
import com.forumapp.service.NotificationService;
import com.forumapp.service.UtilsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final FollowRepository followRepository;
    private final NotificationRepository notificationRepository;
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;
    private final NotificationRealtimeService notificationRealtimeService;
    private final UtilsService utilsService;
    private final ApplicationEventPublisher eventPublisher;




    @Override
    public void changeFollowPost(String slug){
        PostEntity post = postRepository.findBySlug(slug);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if(auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof UserPrincipal up)) {
            throw new RuntimeException("Cần đăng nhập");
        }
        UserEntity user = userRepository.findById(up.getId())
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));
        FollowPostEntity follow = followRepository.findOneByPost_IdAndUser_Id(post.getId(), user.getId());
        if(follow == null){
            follow = FollowPostEntity.builder()
                    .post(post)
                    .user(user)
                    .checked(true)
                    .build();
        }
        else{
            follow.setChecked(!follow.getChecked());
        }
        followRepository.save(follow);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<NotificationResponse> getNotification(Pageable pageable){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if(auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof UserPrincipal up)){
            throw new RuntimeException("Vui lòng đăng nhập");
        }
        Page<NotificationEntity> list = notificationRepository.findByRecipientId(up.getId(), pageable);
        Page<NotificationResponse> result = list.map(data -> NotificationResponse.builder()
                .id(data.getId())
                .username(data.getRecipient().getUsername())
                .avatar(data.getAvatar())
                .title(data.getMessage())
                .date(data.getCreatedAt())
                .message(data.getMessage())
                .slug(data.getLinkUrl())
                .message(data.getMessage())
                .build());

        return result;
    }

    @Override
    @Transactional
    public void checkAllNotification(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if(auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof UserPrincipal up)){
            throw new RuntimeException("Người dùng chưa đăng nhập");
        }
        UserEntity user = userRepository.findById(up.getId()).orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));
        List<NotificationEntity> list = notificationRepository.findAllByRecipientId(user.getId());
        for(NotificationEntity c : list){
            c.setRead(Boolean.TRUE);
        }
        notificationRepository.saveAll(list);
    }

    @Override
    @Transactional
    public void deleteOneNotification(Long id){
        NotificationEntity result = notificationRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Không tìm thấy notification"));
        result.setRead(Boolean.TRUE);
        notificationRepository.save(result);
    }

    @Override
    @Transactional
    public void deleteAllNotification(){

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if(auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof UserPrincipal up)){
            throw new RuntimeException("Người dùng chưa đăng nhập");
        }
        UserEntity user = userRepository.findById(up.getId()).orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));
        notificationRepository.deleteByRecipientId(user.getId());
    }

    @Override
    @Transactional
    public void likePost(Long id){
        PostEntity post = postRepository.findById(id).orElseThrow(() -> new RuntimeException("Bài viết không tồn tại"));
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if(auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof UserPrincipal up)){
            throw new RuntimeException("Người dùng chưa đăng nhập");
        }
        UserEntity user = userRepository.findById(up.getId()).orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));
        LikeEntity like = LikeEntity.builder()
                .post(post)
                .user(user)
                .build();
        likeRepository.saveAndFlush(like);

        eventPublisher.publishEvent(new LikeEvent(user, like));

    }


    @Override
    @Transactional
    public void likeComment(Long id){
        CommentEntity cmt = commentRepository.findById(id).orElseThrow(() -> new RuntimeException("Bình luận không tồn tại"));
        if(cmt == null){
            throw new RuntimeException("Bình luận không tồn tại");
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if(auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof UserPrincipal up)){
            throw new RuntimeException("Người dùng chưa đăng nhập");
        }
        UserEntity user = userRepository.findById(up.getId()).orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));
        LikeEntity like = LikeEntity.builder()
                .comment(cmt)
                .user(user)
                .build();
        likeRepository.saveAndFlush(like);

        eventPublisher.publishEvent(new LikeEvent(user, like));
    }

    @Override
    public void deleteLikePost(Long id){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if(auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof UserPrincipal up)){
            throw new RuntimeException("Người dùng chưa đăng nhập");
        }
        LikeEntity like = likeRepository.findById(id).orElseThrow(() -> new RuntimeException("Like không tồn tại"));
        if(like == null){
            throw new RuntimeException("Like bài viết không tồn tại");
        }
        likeRepository.deleteById(id);

    }

    @Override
    public void deleteLikeComment(Long id){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if(auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof UserPrincipal up)){
            throw new RuntimeException("Người dùng chưa đăng nhập");
        }
        LikeEntity like = likeRepository.findById(id).orElseThrow(() -> new RuntimeException("Like không tồn tại"));
        if(like == null){
            throw new RuntimeException("Like bình luận không tồn tại");
        }
        likeRepository.deleteById(id);
    }

    @Override
    public Page<LikeResponse> getAllLikePost(Long id, Pageable pageable){
        PostEntity post = postRepository.findById(id).orElseThrow(() -> new RuntimeException("Bài viết không tồn tại"));
        Page<LikeEntity> list = likeRepository.findByPost_Id(post.getId(), pageable);
        Page<LikeResponse> result = list.map(like -> LikeResponse.builder()
                .id(like.getId())
                .userId(like.getUser().getId())
                .username(like.getUser().getUsername())
                .postId(post.getId())
                .build());

        return result;
    }


    @Override
    public Page<LikeResponse> getAllLikeComment(Long id, Pageable pageable){
        CommentEntity comment = commentRepository.findById(id).orElseThrow(() -> new RuntimeException("Bình luận không tồn tại"));
        Page<LikeEntity> list = likeRepository.findByComment_Id(comment.getId(), pageable);
        Page<LikeResponse> result = list.map(like -> LikeResponse.builder()
                .id(like.getId())
                .userId(like.getUser().getId())
                .username(like.getUser().getUsername())
                .commentId(comment.getId())
                .build());

        return result;
    }





}
