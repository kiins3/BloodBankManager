package com.blood.controller.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.blood.dto.Admin.*;
import com.blood.service.StatService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/event")
@SecurityRequirement(name = "bearerAuth")
@Slf4j
@RequiredArgsConstructor
public class EventAdminStatController {

    private final StatService statService;

    @GetMapping("/event-stat")
    public ResponseEntity<?> getEventStatusStat() {
        EventStatusStatResponse response = statService.getEventStatusStat();
        return ResponseEntity.ok(response);
    }
}