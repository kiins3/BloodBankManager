package com.blood.controller.admin;

import com.blood.dto.BloodRequest.BloodRequestSummaryResponse;
import com.blood.dto.Hospital.UpdateHospitalForAdminRequest;
import com.blood.dto.Staff.UpdateStaffForAdminRequest;
import com.blood.service.BloodRequestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.blood.dto.Hospital.HospitalResponse;
import com.blood.model.enumformat.UserStatus;
import com.blood.service.HospitalService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/hospital")
@SecurityRequirement(name = "bearerAuth")
@Slf4j
@RequiredArgsConstructor
public class HospitalAdminController {

    private final HospitalService hospitalService;
    private final BloodRequestService bloodRequestService;

    @GetMapping("/get-list-hospital")
    public ResponseEntity<?> getListHospital(
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) UserStatus status,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size) {
        Page<HospitalResponse> response = hospitalService.getAllHospital(keyword, status, PageRequest.of(page, size));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/get-hospital-detail/{hospitalId}")
    public ResponseEntity<?> getHospitalDetail(@PathVariable Integer hospitalId) {
        HospitalResponse response = hospitalService.getHospitalDetail(hospitalId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/update-hospital/{hospitalId}")
    public ResponseEntity<?> updateStaff(@PathVariable Integer hospitalId, @RequestBody UpdateHospitalForAdminRequest rq){
        String response = hospitalService.updateHospitalForAdmin(hospitalId, rq);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/get-history-request/{hospitalId}")
    public ResponseEntity<?> getHistoryRequest(@PathVariable Integer hospitalId,
                                               @RequestParam(defaultValue = "0") int page,
                                               @RequestParam(defaultValue = "10") int size) {
        Page<BloodRequestSummaryResponse> responses = hospitalService.getRequestHistory(hospitalId, PageRequest.of(page, size));
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/get-detail-request/{requestId}")
    public ResponseEntity<?> getDetailRequest(@PathVariable Integer requestId) {
        return ResponseEntity.ok().body(bloodRequestService.getRequestDetail(requestId));
    }
}
