package com.blood.Controller;

import com.blood.DTO.Staff.StaffResponse;
import com.blood.Repository.StaffRepository;
import com.blood.Service.StaffService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/staff")
public class StaffController {

    @Autowired
    private StaffRepository staffRepository;

    @Autowired
    private StaffService staffService;

    @GetMapping("/get-list-staff")
    public ResponseEntity<?>  getAllStaff() {
        try {
            List<StaffResponse> response = staffService.getAllStaff();
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
