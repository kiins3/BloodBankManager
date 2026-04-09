package com.blood.Controller;

import com.blood.DTO.Blood.DonationRequest;
import com.blood.DTO.Donor.DonorResponse;
import com.blood.DTO.Event.ScreeningRequest;
import com.blood.Service.RegistrationService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/staff/registration")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class RegistrationStaffController {
    @Autowired
    private RegistrationService registrationService;

    @GetMapping("/get-all-donors-of-event/{eventId}")
    public ResponseEntity<?> getAllDonorsOfEvent(@PathVariable Integer eventId) {
        try {
            List<DonorResponse> donors = registrationService.getAllDonorsOfEvent(eventId);
            return ResponseEntity.ok(donors);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/checkin/{ticketCode}/{eventId}")
    public ResponseEntity<?> checkin(@PathVariable String ticketCode, @PathVariable Integer eventId) {
        try {
            DonorResponse response = registrationService.checkin(eventId, ticketCode);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return  ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/screening/{regisId}")
    public ResponseEntity<?> screening(@PathVariable Integer regisId, @RequestBody ScreeningRequest rq) {
        try {
            String message = registrationService.saveScreeningResult(regisId, rq);
            return ResponseEntity.ok(message);
        } catch (RuntimeException e) {
            return  ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/donate-blood/{regisId}")
    public ResponseEntity<?> takeBlood(@PathVariable Integer regisId, @RequestBody DonationRequest rq) {
        try {
            String message = registrationService.donateBlood(regisId, rq);
            return ResponseEntity.ok(message);
        } catch (RuntimeException e) {
            return  ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
