package com.blood.Controller;

import com.blood.Service.BloodRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/shared/blood-request")
public class BloodRequestShareAPIController {
    @Autowired
    private BloodRequestService bloodRequestService;

    @GetMapping("/detail/{requestId}")
    public ResponseEntity<?> getRequestDetail(@PathVariable Integer requestId) {
        try {
            return ResponseEntity.ok().body(bloodRequestService.getRequestDetail(requestId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
