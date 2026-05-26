package com.blood.controller;

import com.blood.dto.Auth.ForgetPasswordRequest;
import com.blood.dto.Auth.LoginRequest;
import com.blood.dto.Auth.RegisterRequest;
import com.blood.service.AuthService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest rq) {
        log.info("Login attempt for email: {}", rq.getEmail());
        return authService.login(rq);
    }

    @PostMapping("/sign-up")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest rq) {
        log.info("Registration attempt for email: {}", rq.getEmail());
        return authService.register(rq);
    }

    @PostMapping("/generate-otp")
    public ResponseEntity<?> generateOTP(@RequestBody String email) {
        log.info("Request: Generate OTP for email: {}", email);
        authService.generateAndSendOTP(email);
        return ResponseEntity.ok("Đã gửi mã xác nhận tới email của bạn");
    }

    @PostMapping("/verify-otp-and-change-password")
    public ResponseEntity<?> verifyOTPAndChangePassword(@RequestBody ForgetPasswordRequest rq) {
        log.info("Request: Reset password for email: {}", rq.getEmail());
        authService.verifyOTPAndResetPassword(rq);
        return ResponseEntity.ok("Đổi mật khẩu thành công");
    }
}
