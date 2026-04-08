package com.blood.Controller;

import com.blood.DTO.Blood.DonationRequest;
import com.blood.DTO.Donor.DonorResponse;
import com.blood.DTO.Event.ScreeningRequest;
import com.blood.DTO.Event.TicketResponse;
import com.blood.DTO.Event.TicketSummaryResponse;
import com.blood.Model.Donor;
import com.blood.Model.Users;
import com.blood.Repository.DonorRepository;
import com.blood.Repository.EventRegistrationRepository;
import com.blood.Repository.UserRepository;
import com.blood.Service.RegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/registration")
@RequiredArgsConstructor
public class RegistrationController {
    @Autowired
    private RegistrationService registrationService;

    @Autowired
    private EventRegistrationRepository eventRegistrationRepository;

    @Autowired
    private DonorRepository donorRepository;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/regisEvent/{eventId}")
    public ResponseEntity<?> registration(@PathVariable Integer eventId) {

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();

            Users user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

            Integer donorId = user.getDonor().getDonorId();

            String message = registrationService.registration(eventId, donorId);
            return ResponseEntity.ok(message);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/get-all-my-tickets")
    public ResponseEntity<?> getAllMyTicket() {
        try {
            List<TicketSummaryResponse> tickets = registrationService.getAllMyTickets();
            return ResponseEntity.ok(tickets);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/get-my-ticket/{eventId}")
    public ResponseEntity<?> getTicket(@PathVariable Integer eventId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();

            Users user = userRepository.findByEmail(email).get();
            Integer donorId = user.getDonor().getDonorId();

            TicketResponse ticket = registrationService.getMyTickets(eventId, donorId);

            return ResponseEntity.ok(ticket);
        } catch (RuntimeException e) {
            return  ResponseEntity.badRequest().body(e.getMessage());
        }
    }

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
