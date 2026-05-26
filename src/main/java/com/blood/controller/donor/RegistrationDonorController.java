package com.blood.controller.donor;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.blood.dto.Event.TicketResponse;
import com.blood.dto.Event.TicketSummaryResponse;
import com.blood.model.enumformat.EventRegisStatus;
import com.blood.model.Users;
import com.blood.repository.UserRepository;
import com.blood.service.RegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/donor/registration")
@SecurityRequirement(name = "bearerAuth")
@Slf4j
@RequiredArgsConstructor
public class RegistrationDonorController {
    private final RegistrationService registrationService;
    private final UserRepository userRepository;

    @PostMapping("/regisEvent/{eventId}")
    public ResponseEntity<?> registration(@PathVariable Integer eventId) {

        String message = registrationService.registration(eventId);
        return ResponseEntity.ok(message);
    }

    @GetMapping("/get-all-my-tickets")
    public ResponseEntity<?> getAllMyTicket(@RequestParam(required = false) EventRegisStatus status) {
        List<TicketSummaryResponse> tickets = registrationService.getAllMyTickets(status);
        return ResponseEntity.ok(tickets);
    }

    @GetMapping("/get-my-ticket/{eventId}")
    public ResponseEntity<?> getTicket(@PathVariable Integer eventId) {
        TicketResponse ticket = registrationService.getMyTicketDetail(eventId);
        return ResponseEntity.ok(ticket);
    }

    @PatchMapping("/discard-my-ticket/{registrationId}")
    public ResponseEntity<?> cancelRegistration(@PathVariable Integer registrationId) {
        String message = registrationService.cancelRegistration(registrationId);
        return ResponseEntity.ok(message);
    }


}
