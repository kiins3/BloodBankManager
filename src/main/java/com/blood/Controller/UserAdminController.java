package com.blood.Controller;

import com.blood.DTO.Hospital.CreateHospitalAccountRequest;
import com.blood.DTO.Staff.CreateStaffAccountRequest;
import com.blood.Service.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/user")
@SecurityRequirement(name = "bearerAuth")
public class UserAdminController {

    @Autowired
    private UserService userService;

    @PostMapping("/create-hospital-account")
    public ResponseEntity<?> CreateHospitalAccount(@RequestBody CreateHospitalAccountRequest rq) {
        try {
            String message = userService.createHospitalAccount(rq);
            return ResponseEntity.ok().body(message);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/create-staff-account")
    public ResponseEntity<?> CreateStaffAccount(@RequestBody CreateStaffAccountRequest rq) {
        try {
            String message = userService.createStaffAccount(rq);
            return ResponseEntity.ok().body(message);
        } catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}
