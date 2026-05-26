package com.blood.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.blood.dto.Auth.ChangePasswordRequest;
import com.blood.service.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/shared/user")
@SecurityRequirement(name = "bearerAuth")
@Slf4j
@RequiredArgsConstructor
public class UserSharedAPIController {
    private final UserService userService;

    @GetMapping("/get-profile")
    public ResponseEntity<?> GetMyProfile() {
        return userService.getMyProfile();
    }

    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordRequest rq) {
        userService.changPassword(rq);
        return ResponseEntity.ok("Đổi mật khẩu thành công");
    }
}
