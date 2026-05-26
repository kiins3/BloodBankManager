package com.blood.controller.admin;

import com.blood.dto.Staff.StaffAvailabilityResponse;
import com.blood.dto.Staff.StaffDetailResponse;
import com.blood.dto.Staff.UpdateStaffForAdminRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.blood.dto.Staff.StaffResponse;
import com.blood.model.enumformat.Position;
import com.blood.model.enumformat.UserStatus;
import com.blood.service.StaffService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.hibernate.engine.jdbc.Size;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/staff")
@SecurityRequirement(name = "bearerAuth")
@Slf4j
@RequiredArgsConstructor
public class StaffAdminController {

    private final StaffService staffService;

    @GetMapping("/get-list-staff")
    public ResponseEntity<?> getAllStaff(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Position position,
            @RequestParam(required = false) UserStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

            Page<StaffResponse> response = staffService.getAllStaff(keyword, position, status, PageRequest.of(page, size));
            return ResponseEntity.ok(response);
    }

    @GetMapping("/get-staff-detail/{staffId}")
    public ResponseEntity<?> getStaffDetail(@PathVariable Integer staffId) {
        StaffDetailResponse response =  staffService.getStaffDetail(staffId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/update-staff/{staffId}")
    public ResponseEntity<?> updateStaff(@PathVariable Integer staffId, @RequestBody UpdateStaffForAdminRequest rq){
        String response = staffService.updateStaffForAdmin(staffId, rq);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/get-history-assignment/{staffId}")
    public ResponseEntity<?> getHistoryAssignment(@PathVariable Integer staffId,
                                                  @RequestParam(defaultValue = "0") int page,
                                                  @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(staffService.getStaffHistory(staffId, pageable));
    }

    @GetMapping("/get-smart-list-staff")
    public ResponseEntity<?> getSmartStaffList(){
        List<StaffAvailabilityResponse> response = staffService.getSmartStaffList();
        return ResponseEntity.ok(response);
    }
}
