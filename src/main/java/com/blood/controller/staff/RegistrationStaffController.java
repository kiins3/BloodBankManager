package com.blood.controller.staff;

import lombok.extern.slf4j.Slf4j;

import com.blood.dto.Blood.DonationRequest;
import com.blood.dto.Donor.DonorResponse;
import com.blood.dto.Event.ScreeningRequest;
import com.blood.service.RegistrationService;
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
@Slf4j

public class RegistrationStaffController {
    private final RegistrationService registrationService;

    @GetMapping("/get-all-donors-of-event/{eventId}")
    public ResponseEntity<?> getAllDonorsOfEvent(@PathVariable Integer eventId) {
        List<DonorResponse> donors = registrationService.getAllDonorsOfEvent(eventId);
        return ResponseEntity.ok(donors);
    }

    @PostMapping("/checkin/{ticketCode}/{eventId}")
    public ResponseEntity<?> checkin(@PathVariable String ticketCode, @PathVariable Integer eventId) {
        DonorResponse response = registrationService.checkin(eventId, ticketCode);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/screening/{regisId}")
    public ResponseEntity<?> screening(@PathVariable Integer regisId, @RequestBody ScreeningRequest rq) {
        String message = registrationService.saveScreeningResult(regisId, rq);
        return ResponseEntity.ok(message);
    }

    @PostMapping("/donate-blood/{regisId}")
    public ResponseEntity<?> takeBlood(@PathVariable Integer regisId, @RequestBody DonationRequest rq) {
        String message = registrationService.donateBlood(regisId, rq);
        return ResponseEntity.ok(message);
    }
}
