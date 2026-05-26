package com.blood.controller.staff;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.blood.dto.Admin.*;
import com.blood.model.Staff;
import com.blood.model.Users;
import com.blood.repository.StaffRepository;
import com.blood.repository.UserRepository;
import com.blood.service.StatService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/staff")
@SecurityRequirement(name = "bearerAuth")
@Slf4j
@RequiredArgsConstructor
public class StaffGeneralController {

    private final StatService statService;

    @GetMapping("/general-stat")
    public ResponseEntity<?> getGeneralStat() {
            StaffGeneralStatResponse response = statService.getStaffGeneralStat();
            return ResponseEntity.ok(response);
    }
}