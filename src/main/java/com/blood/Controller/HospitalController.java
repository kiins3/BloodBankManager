package com.blood.Controller;

import com.blood.DTO.Hospital.HospitalResponse;
import com.blood.DTO.Profile.UpdateHospitalProfileRequest;
import com.blood.Service.HospitalService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hospital")
@SecurityRequirement(name = "bearerAuth")
public class HospitalController {
    @Autowired
    HospitalService hospitalService;

    @PutMapping("/update-hospital")
    public void updateHospital(@RequestBody UpdateHospitalProfileRequest rq) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        hospitalService.updateHospitalProfile(email, rq);
    }
}
