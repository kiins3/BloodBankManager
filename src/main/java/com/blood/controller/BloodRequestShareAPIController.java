package com.blood.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.blood.service.BloodRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/shared/blood-request")
@SecurityRequirement(name = "bearerAuth")
@Slf4j
@RequiredArgsConstructor
public class BloodRequestShareAPIController {
    private final BloodRequestService bloodRequestService;

    @GetMapping("/detail/{requestId}")
    public ResponseEntity<?> getRequestDetail(@PathVariable Integer requestId) {
        return ResponseEntity.ok().body(bloodRequestService.getRequestDetail(requestId));
    }
}
