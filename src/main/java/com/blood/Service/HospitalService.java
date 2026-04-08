package com.blood.Service;

import com.blood.DTO.Hospital.HospitalResponse;
import com.blood.DTO.Profile.UpdateHospitalProfileRequest;
import com.blood.Model.Hospital;
import com.blood.Model.Users;
import com.blood.Repository.HospitalRepository;
import com.blood.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class HospitalService {

    @Autowired
    private HospitalRepository hospitalRepository;

    @Autowired
    private UserRepository userRepository;

    public List<HospitalResponse> getAllHospital(){
        List<Hospital>  hospitals = hospitalRepository.findAll();
        return hospitals.stream().map(hos ->
                HospitalResponse.builder()
                        .hospitalName(hos.getHospitalName())
                        .address(hos.getAddress())
                        .hotline(hos.getHotline())
                        .email(hos.getUser().getEmail())
                        .build())
                        .collect(Collectors.toList());
    }

    public void updateHospitalProfile (String email, UpdateHospitalProfileRequest rq) {
        Users user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        Hospital hospital = hospitalRepository.findByUser(user).orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        if (rq.getHospitalName() != null) { hospital.setHospitalName(rq.getHospitalName()); }
        if (rq.getAddress() != null) { hospital.setAddress(rq.getAddress()); }
        if (rq.getHotline() != null) { hospital.setHotline(rq.getHotline()); }

        hospitalRepository.save(hospital);
    }

}
