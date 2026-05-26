package com.blood.controller.staff;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.blood.dto.Admin.MyAssignmentResponse;
import com.blood.model.Staff;
import com.blood.model.Users;
import com.blood.repository.StaffRepository;
import com.blood.repository.UserRepository;
import com.blood.service.AssignmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/staff/assignment")
@SecurityRequirement(name = "bearerAuth")
@Slf4j
@RequiredArgsConstructor
public class AssignmentStaffController {

    private final AssignmentService assignmentService;

    @GetMapping("/my-assignment")
    public ResponseEntity<?> getMyAssignment() {
        MyAssignmentResponse response = assignmentService.getMyAssignment();
        return ResponseEntity.ok(response);
    }
}
