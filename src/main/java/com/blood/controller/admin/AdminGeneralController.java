package com.blood.controller.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.blood.dto.Admin.*;
import com.blood.service.StatService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@SecurityRequirement(name = "bearerAuth")
@Slf4j
@RequiredArgsConstructor
public class AdminGeneralController {

    private final StatService statService;

    @GetMapping("/general-stat")
    public ResponseEntity<?> getGeneralStat() {
        GeneralStatResponse response = statService.getStatForAdmin();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/donor-stat")
    public ResponseEntity<?> getDonorStat() {
        DonorStatForAdminResponse response = statService.getDonorStatForAdmin();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/blood-stat")
    public ResponseEntity<?> getBloodStat() {
        List<?> response = statService.getBloodTypeStat();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/event-stat")
    public ResponseEntity<?> getEventStat() {
        EventStatListResponse response = statService.getEventStat();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/staff-stat")
    public ResponseEntity<?> getStaffStat() {
        StaffStatResponse response = statService.getStaffStat();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/hospital-stat")
    public ResponseEntity<?> getHospitalStat() {
        HospitalStatResponse response = statService.getHospitalStat();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/blood-inventory-stat")
    public ResponseEntity<?> getBloodInventoryStat() {
        BloodInventoryStatResponse response = statService.getBloodInventoryStat();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/storage-equipment-stat")
    public ResponseEntity<?> getStorageEquipmentStat() {
        StorageEquipmentStatResponse response = statService.getStorageEquipmentStat();
        return ResponseEntity.ok(response);
    }
}
