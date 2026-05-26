package com.blood.controller.admin;

import com.blood.dto.Admin.EventAssignmentRequest;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.blood.dto.Admin.AssignStaffRequest;
import com.blood.dto.Admin.AssignmentStaffResponse;
import com.blood.service.AssignmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/assignment")
@SecurityRequirement(name = "bearerAuth")
@Slf4j
@RequiredArgsConstructor
public class AssignmentAdminController {

    private final AssignmentService  assignmentService;

    @GetMapping("/list-assignment-staff/{eventId}")
    public ResponseEntity<?> getAssignmentStaff(@PathVariable Integer eventId) {
        AssignmentStaffResponse response = assignmentService.getAssignmentStaff(eventId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/assignment-staff/{eventId}")
    public ResponseEntity<?> assignStaff(@PathVariable Integer eventId, @RequestBody EventAssignmentRequest request) {
        String result = assignmentService.syncStaffToEvent(request);
        return ResponseEntity.ok(result);
    }

    /*@DeleteMapping("/adjust-assignment/{eventId}")
    public ResponseEntity<?> adjustAssignment(@PathVariable Integer eventId, @RequestBody Integer staffId) {
        String response = assignmentService.removeStaffFromEvent(eventId, staffId);
        return ResponseEntity.ok(response);
    }*/
}
