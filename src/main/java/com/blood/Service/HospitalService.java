package com.blood.Service;

import com.blood.DTO.Profile.UpdateHospitalProfileRequest;
import com.blood.Model.Hospital;
import com.blood.Model.Users;
import com.blood.Repository.HospitalRepository;
import com.blood.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class HospitalService {

    @Autowired
    private HospitalRepository hospitalRepository;

    @Autowired
    private UserRepository userRepository;

    public void updateHospitalProfile (String email, UpdateHospitalProfileRequest rq) {
        Users user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        Hospital hospital = hospitalRepository.findByUser(user).orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        if (rq.getHospitalName() != null) { hospital.setHospitalName(rq.getHospitalName()); }
        if (rq.getAddress() != null) { hospital.setAddress(rq.getAddress()); }

        hospitalRepository.save(hospital);
    }


}
