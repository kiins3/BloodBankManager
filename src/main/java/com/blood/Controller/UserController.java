package com.blood.Controller;

import com.blood.DTO.Hospital.CreateHospitalAccountRequest;
import com.blood.DTO.Staff.CreateStaffAccountRequest;
import com.blood.Repository.DonorRepository;
import com.blood.Repository.UserRepository;
import com.blood.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/get-profile")
    public ResponseEntity<?> GetMyProfile() {
        return userService.getMyProfile();
    }

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
