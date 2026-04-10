package com.blood.Controller;

import com.blood.DTO.Auth.ChangePasswordRequest;
import com.blood.DTO.Auth.ForgetPasswordRequest;
import com.blood.DTO.Auth.LoginRequest;
import com.blood.DTO.Auth.RegisterRequest;
import com.blood.Repository.UserRepository;
import com.blood.Service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest rq) {
        return authService.login(rq);
    }

    @PostMapping("/sign-up")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest rq) {
        return authService.register(rq);
    }

    @PostMapping("/generate-otp")
    public ResponseEntity<?> generateOTP(@RequestBody String email) {
        try {
            authService.generateAndSendOTP(email);
            return ResponseEntity.ok("Đã gửi mã xác nhận tới email của bạn");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/verify-otp-and-change-password")
    public ResponseEntity<?> verifyOTPAndChangePassword(@RequestBody ForgetPasswordRequest rq) {
        try {
            authService.verifyOTPAndResetPassword(rq);
            return ResponseEntity.ok("Đổi mật khẩu thành công");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}
