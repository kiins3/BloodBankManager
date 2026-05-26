package com.blood.controller.hospital;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.blood.dto.Profile.UpdateHospitalProfileRequest;
import com.blood.dto.Hospital.HospitalDashboardStatResponse;
import com.blood.service.HospitalService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/hospital")
@SecurityRequirement(name = "bearerAuth")
@Slf4j
@RequiredArgsConstructor
public class HospitalController {
    private final HospitalService hospitalService;

    @PutMapping("/update-hospital")
    public void updateHospital(@RequestBody UpdateHospitalProfileRequest rq) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        hospitalService.updateHospitalProfile(email, rq);
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getHospitalStats() {
        HospitalDashboardStatResponse stats = hospitalService.hospitalStat();
        return ResponseEntity.ok().body(stats);
    }
}
