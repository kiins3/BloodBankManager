package com.blood.controller.staff;

import com.blood.dto.Staff.UpdateStaffRequest;
import com.blood.service.StaffService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@RequestMapping("/api/staff/staff")
public class StaffController {
    private final StaffService staffService;

    @PatchMapping("/update-profile")
    public ResponseEntity<?> UpdateProfile(@RequestBody UpdateStaffRequest rq){
        String response = staffService.updateStaff(rq);
        return ResponseEntity.ok(response);
    }
}
