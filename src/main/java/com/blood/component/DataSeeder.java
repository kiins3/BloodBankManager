package com.blood.component;

import com.blood.model.Users;
import com.blood.model.enumformat.Role;
import com.blood.model.enumformat.UserStatus;
import com.blood.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        boolean hasAdmin = userRepository.existsByRole(Role.ADMIN);

        if (!hasAdmin) {
            log.info("CẢNH BÁO: Không tìm thấy tài khoản Admin nào. Đang tiến hành tạo Root Admin mặc định...");

            Users rootAdmin = new Users();
            rootAdmin.setEmail("hethongmau@gmail.com");
            rootAdmin.setPassword(passwordEncoder.encode("123456"));
            rootAdmin.setRole(Role.ADMIN);
            rootAdmin.setStatus(UserStatus.ACTIVE);
            userRepository.save(rootAdmin);

            log.info("Đã tạo thành công Root Admin. Email: hethongmau@gmail.com | Password: 123456");
            log.info("Vui lòng đăng nhập và đổi mật khẩu ngay sau lần sử dụng đầu tiên!");
        }
    }
}
