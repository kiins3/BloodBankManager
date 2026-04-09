package com.blood.Controller;

import com.blood.DTO.Profile.UpdateDonorProfileRequest;
import com.blood.Service.DonorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/donor")
public class DonorController {
    @Autowired
    private DonorService donorService;

    @PutMapping("/update-donor")
    public ResponseEntity<?> updateDonor(@RequestBody UpdateDonorProfileRequest rq) {
        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            String message = donorService.updateDonorProfile(email, rq);
            return ResponseEntity.ok().body(message);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
