package com.blood.Service;

import com.blood.DTO.Auth.ChangePasswordRequest;
import com.blood.DTO.Hospital.CreateHospitalAccountRequest;
import com.blood.DTO.Hospital.HospitalResponse;
import com.blood.DTO.Profile.GetDonorProfileResponse;
import com.blood.DTO.Profile.GetHospitalProfileResponse;
import com.blood.DTO.Profile.UpdateDonorProfileRequest;
import com.blood.DTO.Profile.UpdateHospitalProfileRequest;
import com.blood.DTO.Staff.CreateStaffAccountRequest;
import com.blood.Model.Donor;
import com.blood.Model.Hospital;
import com.blood.Model.Staff;
import com.blood.Model.Users;
import com.blood.Repository.DonorRepository;
import com.blood.Repository.HospitalRepository;
import com.blood.Repository.StaffRepository;
import com.blood.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DonorRepository donorRepository;

    @Autowired
    private HospitalRepository hospitalRepository;

    @Autowired
    private StaffRepository staffRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public ResponseEntity<?> getMyProfile() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Users user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));


        if (user.getRole().equalsIgnoreCase("DONOR")) {
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
        } else if (user.getRole().equals("HOSPITAL")) {
            Hospital hospital = hospitalRepository.findByUser(user).orElseThrow (()-> new RuntimeException("Không tìm thấy người dùng"));

            GetHospitalProfileResponse response = new GetHospitalProfileResponse();
            response.setHospitalId(hospital.getHospitalId());
            response.setEmail(user.getEmail());
            response.setHospitalName(hospital.getHospitalName());
            response.setHotline(hospital.getHotline());
            response.setAddress(hospital.getAddress());

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
        user.setRole("HOSPITAL");
        user.setStatus("ACTIVE");
        hospital.setUser(user);
        hospital.setHospitalName(rq.getHospitalName());
        hospital.setAddress(rq.getAddress());
        hospital.setHotline(rq.getHotline());

        userRepository.save(user);
        hospitalRepository.save(hospital);

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
        user.setStatus("ACTIVE");
        Staff staff = new Staff();
        staff.setUser(user);
        staff.setFullName(rq.getFullName());
        staff.setPhone(rq.getPhone());
        staff.setGender(rq.getGender());
        staff.setDob(rq.getDob());
        staff.setPosition(rq.getPosition());
        staff.setCccd(rq.getCccd());
        staff.setStatus(rq.getStatus());
        userRepository.save(user);
        staffRepository.save(staff);

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
    }
}
