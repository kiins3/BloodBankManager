package com.blood.Controller;

import com.blood.DTO.Hospital.HospitalResponse;
import com.blood.DTO.Profile.UpdateDonorProfileRequest;
import com.blood.DTO.Profile.UpdateHospitalProfileRequest;
import com.blood.Service.HospitalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/hospital")
public class HospitalController {

    @Autowired
    HospitalService hospitalService;

    @GetMapping("/get-list-hospital")
    public ResponseEntity<?> getListHospital(){
        try {
            List<HospitalResponse> response = hospitalService.getAllHospital();
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/update-hospital")
    public void updateHospital(@RequestBody UpdateHospitalProfileRequest rq) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        hospitalService.updateHospitalProfile(email, rq);
    }
}
