package com.forumapp.api;

import com.forumapp.model.response.ApiResponse;
import com.forumapp.model.response.NotificationResponse;
import com.forumapp.service.NotificationService;
import com.forumapp.service.impl.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("api/utils")
@RequiredArgsConstructor
public class UtilsController {

    private final CloudinaryService cloudinaryService;
    private final NotificationService notificationService;


    @GetMapping("/all-notification")
    public ResponseEntity<Page<NotificationResponse>> getNotification(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable){
        return ResponseEntity.ok(notificationService.getNotification(pageable));

    }

    @PostMapping("/upload-image")
    public ResponseEntity<ApiResponse<String>> uploadImage(MultipartFile file){
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .status(201)
                .message("Tải ảnh thành công")
                .data(cloudinaryService.uploadImage(file))
                .build());
    }

    @PostMapping("/like-post/{id}")
    public ResponseEntity<ApiResponse<Void>> likePost(@PathVariable Long id){
        notificationService.likePost(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .status(201)
                .message("Thích bài viết thành công")
                .build());
    }

    @PostMapping("/like-comment/{id}")
    public ResponseEntity<ApiResponse<Void>> likeComment(@PathVariable Long id){
        notificationService.likeComment(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .status(201)
                .message("Thích bình luận thành công")
                .build());
    }



    @PutMapping("/check-notification")
    public ResponseEntity<ApiResponse<Void>> checkAllNotification(){
        notificationService.checkAllNotification();
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .status(200)
                .message("Đã đọc hết các thông báo")
                .build());
    }

    @DeleteMapping("/notitication/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteOneNotification(@PathVariable Long id){
        notificationService.deleteOneNotification(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .status(200)
                .message("Xóa thông báo thành công")
                .build());
    }

    @DeleteMapping("/notification/all")
    public ResponseEntity<ApiResponse<Void>> deleteAllNotification(){
        notificationService.deleteAllNotification();
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .status(200)
                .message("Đã xóa tất cả thông báo")
                .build());
    }

    @DeleteMapping("/unlike-post/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteLikePost(@PathVariable Long id){
        notificationService.deleteLikePost(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .status(201)
                .message("Xóa thích bài viết thành công")
                .build());
    }

    @DeleteMapping("/unlike-comment/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteLikeComment(@PathVariable Long id){
        notificationService.deleteLikeComment(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .status(201)
                .message("Xóa thích bình luận thành công")
                .build());
    }




}
