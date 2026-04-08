package com.blood.Controller;

import com.blood.DTO.Donor.DonorCheckedInResponse;
import com.blood.DTO.Donor.GetListDonorResponse;
import com.blood.DTO.Donor.VisitorRegistRequest;
import com.blood.DTO.Profile.UpdateDonorProfileRequest;
import com.blood.Service.DonorService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/donor")
@SecurityRequirement(name = "bearerAuth")
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

    @GetMapping("/get-list-donor")
    public List<GetListDonorResponse> getListDonor(){
        return donorService.getAllDonors();
    }

    @GetMapping("/get-list-donor-status/{eventId}")
    public List<DonorCheckedInResponse> getListDonor(@PathVariable Integer eventId, @RequestBody String status){
        return donorService.getListDonorsByStatus(eventId, status);
    }

    @PostMapping("/regist-for-visitor/{eventId}")
    public ResponseEntity<?> registForVisitor(@PathVariable Integer eventId, @RequestBody VisitorRegistRequest rq){
        try {
            String message = donorService.registForVisitor(eventId, rq);
            return ResponseEntity.ok().body(message);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/call-for-blood")
    public ResponseEntity<?> callForBlood(@RequestParam String bloodType,
                                          @RequestParam String rhFactor){
        try {
            String message = donorService.callForBloodDonation(bloodType, rhFactor);
            return ResponseEntity.ok().body(message);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
