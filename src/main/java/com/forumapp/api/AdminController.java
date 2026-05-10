package com.forumapp.api;

import com.forumapp.model.response.ApiResponse;
import com.forumapp.model.response.UserResponse;
import com.forumapp.service.UserService;
import lombok.AllArgsConstructor;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;

    @GetMapping("/users")
    public ResponseEntity<Page<UserResponse>> getAllUser(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.ASC) Pageable pageable){
        return ResponseEntity.ok(userService.getAllUsers(search,status,pageable));
    }

    @PutMapping("/{id}/toggle-status")
    public ResponseEntity<ApiResponse<Void>> toggleOne(@PathVariable Long id){
        userService.editStatusAccount(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .status(200)
                .message("Cập nhật thành công")
                .build());
    }

    @PutMapping("/bulk-toggle-status")
    public ResponseEntity<ApiResponse<Void>> toggleBulk(@RequestBody List<Long> ids){
        userService.editMoreStatusAccount(ids);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .status(200)
                .message("Cập nhật thành công")
                .build());
    }


}
