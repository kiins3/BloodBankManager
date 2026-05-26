package com.blood.service;

import com.blood.dto.Staff.StaffProfileResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.blood.dto.Auth.ChangePasswordRequest;
import com.blood.dto.Hospital.CreateHospitalAccountRequest;
import com.blood.dto.Profile.GetDonorProfileResponse;
import com.blood.dto.Profile.GetHospitalProfileResponse;
import com.blood.dto.Staff.CreateStaffAccountRequest;
import com.blood.model.*;
import com.blood.model.enumformat.Role;
import com.blood.model.enumformat.UserStatus;
import com.blood.repository.DonorRepository;
import com.blood.repository.HospitalRepository;
import com.blood.repository.StaffRepository;
import com.blood.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final DonorRepository donorRepository;
    private final HospitalRepository hospitalRepository;
    private final StaffRepository staffRepository;
    private final PasswordEncoder passwordEncoder;

    public ResponseEntity<?> getMyProfile() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Users user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));


        if (user.getRole() == Role.DONOR) {
            Donor donor = donorRepository.findByUser(user).orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

            GetDonorProfileResponse response = new GetDonorProfileResponse();
            response.setEmail(user.getEmail());
            response.setFullName(donor.getFullName());
            response.setCccd(donor.getCccd());
            response.setGender(donor.getGender());
            response.setPhone(donor.getPhone());
            response.setBloodType(donor.getBloodType());
            response.setRhFactor(donor.getRhFactor());
            response.setDob(donor.getDob());
            response.setAddress(donor.getAddress());
            response.setStatus(user.getStatus());

            return ResponseEntity.ok(response);
        } else if (user.getRole() == Role.HOSPITAL) {
            Hospital hospital = hospitalRepository.findByUser(user).orElseThrow (()-> new RuntimeException("Không tìm thấy người dùng"));

            GetHospitalProfileResponse response = new GetHospitalProfileResponse();
            response.setHospitalId(hospital.getHospitalId());
            response.setEmail(user.getEmail());
            response.setHospitalName(hospital.getHospitalName());
            response.setHotline(hospital.getHotline());
            response.setAddress(hospital.getAddress());

            return ResponseEntity.ok(response);
        } else if (user.getRole() == Role.STAFF_INVENTORY || user.getRole() == Role.STAFF_TECH || user.getRole() == Role.ADMIN) {
            Staff staff = staffRepository.findByUser(user).orElseThrow (()-> new RuntimeException("Không tìm thấy người dùng"));
            StaffProfileResponse response = StaffProfileResponse.builder()
                    .staffId(staff.getStaffId())
                    .address(staff.getAddress())
                    .dob(staff.getDob())
                    .gender(staff.getGender())
                    .email(staff.getUser() != null ? staff.getUser().getEmail() : "Chưa liên kết tài khoản")
                    .fullName(staff.getFullName())
                    .position(staff.getPosition())
                    .phone(staff.getPhone())
                    .status(staff.getStatus())
                    .cccd(staff.getCccd())
                    .build();

            return ResponseEntity.ok(response);
        }

        return ResponseEntity.badRequest().body("Hành động này không được thực hiện");
    }

    @Transactional
    public String createHospitalAccount(CreateHospitalAccountRequest rq){
        if (userRepository.existsByEmail(rq.getEmail())){
            throw new RuntimeException("Email đã tồn tại");
        }

        Users user = new Users();
        Hospital hospital = new Hospital();
        user.setEmail(rq.getEmail());
        user.setPassword(passwordEncoder.encode("123456"));
        user.setCreatedAt(LocalDateTime.now());
        user.setRole(Role.HOSPITAL);
        user.setStatus(UserStatus.ACTIVE);
        hospital.setUser(user);
        hospital.setHospitalName(rq.getHospitalName());
        hospital.setAddress(rq.getAddress());
        hospital.setHotline(rq.getHotline());

        userRepository.save(user);
        log.info("CREATE/UPDATE: State change successfully saved for entity: {}", user);
        hospitalRepository.save(hospital);
        log.info("CREATE/UPDATE: State change successfully saved for entity: {}", hospital);

        return "Đã thêm bệnh viện mới";
    }

    @Transactional
    public String createStaffAccount(CreateStaffAccountRequest rq){
        if (userRepository.existsByEmail(rq.getEmail())){
            throw new RuntimeException("Email đã tồn tại");
        }

        Users user = new Users();
        user.setEmail(rq.getEmail());
        user.setPassword(passwordEncoder.encode("123456"));
        user.setCreatedAt(LocalDateTime.now());
        user.setRole(rq.getRole());
        user.setStatus(UserStatus.ACTIVE);
        Staff staff = new Staff();
        staff.setUser(user);
        staff.setFullName(rq.getFullName());
        staff.setPhone(rq.getPhone());
        staff.setGender(rq.getGender());
        staff.setDob(rq.getDob());
        staff.setPosition(rq.getPosition());
        staff.setCccd(rq.getCccd());
        staff.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);
        log.info("CREATE/UPDATE: State change successfully saved for entity: {}", user);
        staffRepository.save(staff);
        log.info("CREATE/UPDATE: State change successfully saved for entity: {}", staff);

        return "Đã thêm nhân viên mới";
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
        log.info("CREATE/UPDATE: State change successfully saved for entity: {}", user);
    }
}
