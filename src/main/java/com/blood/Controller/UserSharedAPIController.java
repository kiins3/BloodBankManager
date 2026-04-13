package com.blood.Controller;

import com.blood.DTO.Auth.ChangePasswordRequest;
import com.blood.DTO.Hospital.CreateHospitalAccountRequest;
import com.blood.DTO.Staff.CreateStaffAccountRequest;
import com.blood.Service.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/shared/user")
@SecurityRequirement(name = "bearerAuth")
public class UserSharedAPIController {
    @Autowired
    private UserService userService;

    @GetMapping("/get-profile")
    public ResponseEntity<?> GetMyProfile() {
        try{
            return userService.getMyProfile();
        } catch (RuntimeException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest rq) {
        try {
            userService.changPassword(rq);
            return ResponseEntity.ok("Đổi mật khẩu thành công");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
