package com.forumapp.api;


import com.cloudinary.Api;
import com.forumapp.model.request.PostRequest;
import com.forumapp.model.response.ApiResponse;
import com.forumapp.model.response.CommentResponse;
import com.forumapp.model.response.PostResponse;
import com.forumapp.model.response.StatisticResponse;
import com.forumapp.service.NotificationService;
import com.forumapp.service.PostService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/post")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final NotificationService notificationService;

    @GetMapping("/statistic")
    public ResponseEntity<ApiResponse<StatisticResponse>> getStatisticForum(){
        return ResponseEntity.ok(ApiResponse.<StatisticResponse>builder()
                .status(200)
                .data(postService.getStatisticForum())
                .build());
    }

    @GetMapping("/new-thread")
    public ResponseEntity<List<PostResponse>> findTop5PostNewest(){
        return ResponseEntity.ok(postService.findTop5PostNewest());
    }

    @GetMapping("/admin-new-thread")
    public ResponseEntity<List<PostResponse>> findTop5PostNewestAdmin(){
        return ResponseEntity.ok(postService.findTop5PostByAdmin());
    }

    @GetMapping("/{slug}")
    public ResponseEntity<ApiResponse<PostResponse>> getDetailPost(
            @PathVariable String slug,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.ASC) Pageable pageable,
            HttpServletRequest request,
            HttpServletResponse response) {
        return ResponseEntity.ok(ApiResponse.<PostResponse>builder()
                .status(200)
                .message("Chi tiet bai dang")
                .data(postService.getDetailPost(slug, pageable, request,response))
                .build());
    }

    @GetMapping("/lists")
    public ResponseEntity<Page<PostResponse>> findByUserId(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.ASC) Pageable pageable
            ){
        return ResponseEntity.ok(postService.findByUserId(pageable));
    }

    @GetMapping("/list-post-follow")
    public ResponseEntity<Page<PostResponse>> getAllPostFollow(
            @PageableDefault(size = 10) Pageable pageable
    ){
        return ResponseEntity.ok(postService.getAllPostFollow(pageable));
    }
    @PostMapping("/follow-post/{slug}")
    public ResponseEntity<ApiResponse<Void>> changeFollowPost(@PathVariable String slug){
        notificationService.changeFollowPost(slug);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .status(200)
                .build());
    }


    @PostMapping("/create/{slug}")
    public ResponseEntity<ApiResponse<PostResponse>> createPost(@RequestBody PostRequest request,
                                                                @PathVariable String slug){
        return ResponseEntity.ok(ApiResponse.<PostResponse>builder()
                .status(201)
                .data(postService.createPost(slug, request))
                .build());
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse<PostResponse>> editPost(
            @PathVariable Long id,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.ASC) Pageable pageable,
            @RequestBody PostRequest request) {
        return ResponseEntity.ok(ApiResponse.<PostResponse>builder()
                .status(200)
                .message("Chi tiet bai dang")
                .data(postService.editPost(id, request, pageable))
                .build());
    }

    @PutMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePost(@PathVariable Long id){
        postService.deletePost(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .status(200)
                .message("Xóa bài đăng thành công")
                .build());
    }

    @PutMapping("/locked/{id}")
    public ResponseEntity<ApiResponse<Void>> lockedPost(@PathVariable Long id){
        postService.lockedPost(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .status(200)
                .message("Khóa bình luận thành công")
                .build());
    }




}
