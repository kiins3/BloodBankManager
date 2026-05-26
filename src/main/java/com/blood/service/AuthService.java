package com.blood.service;

import com.blood.dto.Auth.*;
import com.blood.model.*;
import com.blood.model.enumformat.Role;
import com.blood.model.enumformat.UserStatus;
import com.blood.repository.DonorRepository;
import com.blood.repository.HospitalRepository;
import com.blood.repository.StaffRepository;
import com.blood.repository.UserRepository;
import com.blood.security.JwtTokenProvider;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final DonorRepository donorRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final StringRedisTemplate redisTemplate;

    public ResponseEntity<?> login(LoginRequest rq) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            rq.getEmail(), rq.getPassword())
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            Users user = userRepository.findByEmail(rq.getEmail())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));

            String jwt = jwtTokenProvider.generateToken(user);
            return ResponseEntity.ok(new JwtResponse(jwt));

        } catch (org.springframework.security.core.AuthenticationException e) {
            log.warn("Login failed for email {}: Bad credentials", rq.getEmail());

            Map<String, String> response = new HashMap<>();
            response.put("message", "Email hoặc mật khẩu không chính xác!");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);

        } catch (Exception e) {
            log.error("Unexpected error during login for email {}: {}", rq.getEmail(), e.getMessage(), e);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Lỗi máy chủ khi đăng nhập");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @Transactional
    public ResponseEntity<?> register(RegisterRequest rq) {
        if (userRepository.existsByEmail(rq.getEmail())) {
            return ResponseEntity.badRequest().body("Email đã tồn tại");
        }

        Users user = new Users();
        user.setEmail(rq.getEmail());
        user.setPassword(passwordEncoder.encode("123456"));
        user.setRole(Role.DONOR);
        user.setStatus(UserStatus.ACTIVE);
        user.setCreatedAt(LocalDateTime.now());

        Users savedUser = userRepository.save(user);

        Donor donor = new Donor();
        donor.setUser(savedUser);
        donor.setFullName(rq.getName());
        donor.setEmail(rq.getEmail());
        donor.setCccd(rq.getCccd());
        donor.setPhone(rq.getPhone());
        donor.setStatus(UserStatus.ACTIVE);
        donorRepository.save(donor);
        
        log.info("CREATE: Successfully registered new User and Donor with email: {}", savedUser.getEmail());

        if (rq.getEmail() != null && !rq.getEmail().isEmpty()) {
            emailService.sendEmail(rq.getEmail(), "HỆ THỐNG NGÂN HÀNG MÁU THÔNG BÁO", "Bạn đã đăng ký tài khoản thành công.\n Mật khẩu của bạn là 123456.\n Vui lòng đăng nhập để cập nhật thông tin cá nhân và đổi mật khẩu để đảm bảo tính bảo mật.");
        } else {
            log.warn("THONG BAO: Khong gui mail vi Email bi NULL hoac Rong cho dang ky CCcD: {}", rq.getCccd());
        }

        return ResponseEntity.ok("Đăng ký thành công");
    }

    public void generateAndSendOTP(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new RuntimeException("Không được để trống email");
        }

        userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Email không đúng hoặc chưa được đăng ký"));

        String cooldownKey = "otp_cooldown:" + email;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(cooldownKey))) {
            throw new RuntimeException("Vui lòng đợi 60 giây trước khi yêu cầu gửi lại mã OTP.");
        }
        String otp = String.format("%06d", new Random().nextInt(999999));

        redisTemplate.opsForValue().set(email, otp, Duration.ofMinutes(5));
        redisTemplate.opsForValue().set(cooldownKey, "blocked", Duration.ofSeconds(60));

        String subject = "ĐẶT LẠI MẬT KHẨU";
        String emailContent = "Mã xác nhận đặt lại mật khẩu của bạn là: " + otp + "\n"
                            + "Mã này sẽ tự động hết hạn sau 5 phút.";

        emailService.sendEmail(email, subject, emailContent);
    }

    public void verifyOTPAndResetPassword(ForgetPasswordRequest rq) {
        String savedOtp = redisTemplate.opsForValue().get(rq.getEmail());

        if (savedOtp == null) {
            throw new RuntimeException("Mã OTP không chính xác");
        }

        if (rq.getNewPassword() == null || rq.getNewPassword().length() < 6) {
            throw new RuntimeException("Mật khẩu mới phải có ít nhất 6 ký tự!");
        }

        Users user = userRepository.findByEmail(rq.getEmail()).orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        user.setPassword(passwordEncoder.encode(rq.getNewPassword()));
        userRepository.save(user);
        log.info("UPDATE: Password successfully reset for user email: {}", user.getEmail());

        redisTemplate.delete(rq.getEmail());
    }
}
