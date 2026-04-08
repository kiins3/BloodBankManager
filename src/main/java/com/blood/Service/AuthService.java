package com.blood.Service;

import com.blood.DTO.Auth.ChangePasswordRequest;
import com.blood.DTO.Auth.JwtResponse;
import com.blood.DTO.Auth.LoginRequest;
import com.blood.DTO.Auth.RegisterRequest;
import com.blood.Model.Donor;
import com.blood.Model.Hospital;
import com.blood.Model.Staff;
import com.blood.Model.Users;
import com.blood.Repository.DonorRepository;
import com.blood.Repository.HospitalRepository;
import com.blood.Repository.StaffRepository;
import com.blood.Repository.UserRepository;
import com.blood.Security.JwtTokenProvider;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

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

    public ResponseEntity<?> login(LoginRequest rq) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        rq.getEmail(), rq.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        Users user = userRepository.findByEmail(rq.getEmail()).get();
        String jwt = jwtTokenProvider.generateToken(user);
        return ResponseEntity.ok(new JwtResponse(jwt));
    }

    @Transactional
    public ResponseEntity<?> register(RegisterRequest rq) {
        if (userRepository.existsByEmail(rq.getEmail())) {
            return ResponseEntity.badRequest().body("Email đã tồn tại");
        }

        Users user = new Users();
        user.setEmail(rq.getEmail());
        user.setPassword(passwordEncoder.encode("123456"));
        user.setRole("DONOR");
        user.setStatus("ACTIVE");
        user.setCreatedAt(LocalDateTime.now());

        Users savedUser = userRepository.save(user);

        Donor donor = new Donor();
        donor.setUser(savedUser);
        donor.setFullName(rq.getName());
        donor.setEmail(rq.getEmail());
        donor.setCccd(rq.getCccd());
        donor.setPhone(rq.getPhone());
        donor.setStatus("ACTIVE");
        donorRepository.save(donor);

        return ResponseEntity.ok("Đăng ký thành công");
    }

    public void changPassword (ChangePasswordRequest rq) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Users user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        if (!passwordEncoder.matches(rq.getOldPassword(), user.getPassword())) {
            throw new RuntimeException("Mật khẩu cũ không đúng");
        }
        if (passwordEncoder.matches(rq.getNewPassword(), user.getPassword())) {
            throw new RuntimeException("Mật khẩu mới phải khác mật khẩu cũ");
        }
        if (!rq.getNewPassword().equals(rq.getConfirmPassword())) {
            throw new RuntimeException("Mật khẩu xác nhận phải giống mật khẩu mới");
        }

        user.setPassword(passwordEncoder.encode(rq.getNewPassword()));
        userRepository.save(user);
    }

}
