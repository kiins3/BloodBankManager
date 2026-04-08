package com.blood.Controller;

import com.blood.DTO.Auth.ChangePasswordRequest;
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


}
