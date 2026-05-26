package com.blood.controller.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.blood.dto.Hospital.CreateHospitalAccountRequest;
import com.blood.dto.Staff.CreateStaffAccountRequest;
import com.blood.service.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/user")
@SecurityRequirement(name = "bearerAuth")
@Slf4j
@RequiredArgsConstructor
public class UserAdminController {

    private final UserService userService;

    @PostMapping("/create-hospital-account")
    public ResponseEntity<?> CreateHospitalAccount(@RequestBody CreateHospitalAccountRequest rq) {
        String message = userService.createHospitalAccount(rq);
        return ResponseEntity.ok().body(message);
    }

    @PostMapping("/create-staff-account")
    public ResponseEntity<?> CreateStaffAccount(@RequestBody CreateStaffAccountRequest rq) {
            String message = userService.createStaffAccount(rq);
            return ResponseEntity.ok().body(message);
    }
}
