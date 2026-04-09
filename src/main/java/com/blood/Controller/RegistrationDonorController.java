package com.blood.Controller;

import com.blood.DTO.Event.TicketResponse;
import com.blood.DTO.Event.TicketSummaryResponse;
import com.blood.Model.Users;
import com.blood.Repository.UserRepository;
import com.blood.Service.RegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/donor/registration")
public class RegistrationDonorController {
    @Autowired
    private RegistrationService registrationService;

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

}
