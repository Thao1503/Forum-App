package com.forumapp.api;

import com.forumapp.model.request.CommentRequest;
import com.forumapp.model.response.ApiResponse;
import com.forumapp.model.response.CommentResponse;
import com.forumapp.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/comment")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @GetMapping("/new-comment")
    public ResponseEntity<List<CommentResponse>> findTop5CommentNewest(){
        return ResponseEntity.ok(commentService.findTop5CommentNewest());
    }

    @PostMapping("/create/{slug}")
    public ResponseEntity<ApiResponse<CommentResponse>> commentPost(@PathVariable String slug,
                                                                    @RequestBody CommentRequest request){
        return  ResponseEntity.ok(ApiResponse.<CommentResponse>builder()
                .status(201)
                .message("Bình luận thành công")
                .data(commentService.commentPost(slug, request))
                .build());
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse<CommentResponse>> editComment(@PathVariable Long id,
                                                                    @RequestBody String content){
        return ResponseEntity.ok(ApiResponse.<CommentResponse>builder()
                .status(200)
                .message("Chỉnh sửa bình luận thành công")
                .data(commentService.editComment(id, content))
                .build());
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(@PathVariable Long id) {
        commentService.deleteComment(id);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .status(200)
                        .message("Xóa bình luận thành công")
                        .data(null)
                        .build()
        );
    }
}
