package com.blood.Controller;

import com.blood.DTO.Profile.UpdateDonorProfileRequest;
import com.blood.DTO.Profile.UpdateHospitalProfileRequest;
import com.blood.Service.HospitalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/hospital")
public class HospitalController {

    @Autowired
    HospitalService hospitalService;

    @PutMapping("/update-hospital")
    public void updateDonor(@RequestBody UpdateHospitalProfileRequest rq) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        hospitalService.updateHospitalProfile(email, rq);
    }
}
