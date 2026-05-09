package com.forumapp.api;

import com.forumapp.model.request.ChangePasswordRequest;
import com.forumapp.model.request.PasswordRequest;
import com.forumapp.model.response.ApiResponse;
import com.forumapp.model.response.ProfileResponse;
import com.forumapp.model.response.SearchingResponse;
import com.forumapp.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<ProfileResponse>> getProfile(){
        return ResponseEntity.ok(ApiResponse.<ProfileResponse>builder()
                .status(200)
                .message("Thông tin cá nhân")
                .data(userService.getProfileUser())
                .build());
    }

    @GetMapping("/search/{search}")
    public ResponseEntity<Page<SearchingResponse>> searchingHome(
            @PathVariable String search,
            @PageableDefault(size = 10) Pageable pageable
            ){
        return ResponseEntity.ok(userService.searchingHome(search, pageable));
    }

    @PutMapping("/avatar")
    public ResponseEntity<ApiResponse<Void>> updateAvatar(@RequestBody String avatar){
        userService.updateAvatar(avatar);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .status(200)
                .message("Cập nhật ảnh đại diện thành công")
                .build());
    }

    @PutMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(@RequestBody ChangePasswordRequest request){
        userService.changePassword(request);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .status(200)
                .message("Đổi mật khẩu thành công")
                .build());
    }
}
