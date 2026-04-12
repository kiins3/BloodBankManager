package com.blood.Service;

import com.blood.DTO.Auth.*;
import com.blood.Model.*;
import com.blood.Repository.DonorRepository;
import com.blood.Repository.HospitalRepository;
import com.blood.Repository.StaffRepository;
import com.blood.Repository.UserRepository;
import com.blood.Security.JwtTokenProvider;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Random;

@Service
public class AuthService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DonorRepository donorRepository;

    @Autowired
    private HospitalRepository hospitalRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private StaffRepository staffRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;
    @Autowired
    private StringRedisTemplate redisTemplate;

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
            e.printStackTrace();
            java.util.Map<String, String> response = new java.util.HashMap<>();
            response.put("message", "Email hoặc mật khẩu không chính xác!");
            return ResponseEntity.status(401).body(response);
        } catch (Exception e) {
            e.printStackTrace();
            java.util.Map<String, String> response = new java.util.HashMap<>();
            response.put("message", "Lỗi máy chủ khi đăng nhập");
            return ResponseEntity.status(500).body(response);
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

        return ResponseEntity.ok("Đăng ký thành công");
    }

    public void generateAndSendOTP(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new RuntimeException("Không được để trống email");
        }

        userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Email không đúng hoặc chưa được đăng ký"));

        String otp = String.format("%06d", new Random().nextInt(999999));

        redisTemplate.opsForValue().set(email, otp, Duration.ofMinutes(5));

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

        redisTemplate.delete(rq.getEmail());
    }
}
