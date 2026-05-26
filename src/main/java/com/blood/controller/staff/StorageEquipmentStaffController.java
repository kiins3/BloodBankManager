package com.blood.controller.staff;

import com.blood.dto.StorageEquipment.ReportBrokenRequest;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.blood.dto.StorageEquipment.ListStorageEquipmentResponse;
import com.blood.model.enumformat.ProductType;
import com.blood.service.StorageEquipmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/staff/storage-equipment")
@SecurityRequirement(name = "bearerAuth")
@Slf4j
@RequiredArgsConstructor
public class StorageEquipmentStaffController {
    private final StorageEquipmentService storageEquipmentService;

    @GetMapping("/list-equipment")
    public ResponseEntity<?> getStorageEquipmentList(@RequestParam(required = false) Integer bloodBagId,
                                                     @RequestParam(required = false) ProductType productType) {
        List<ListStorageEquipmentResponse> list = storageEquipmentService.getListStorageEquipment(bloodBagId, productType);
        return ResponseEntity.ok().body(list);
    }

    @PostMapping("/{equipmentId}/report-broken")
    public ResponseEntity<?> reportBrokenEquipment(
            @PathVariable Integer equipmentId,
            @RequestBody ReportBrokenRequest rq) {

        if (rq.getReason() == null || rq.getReason().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Vui lòng nhập lý do báo hỏng thiết bị.");
        }

        String message = storageEquipmentService.reportBrokenEquipment(equipmentId, rq);
        return ResponseEntity.ok(message);
    }

    @GetMapping("/active")
    public ResponseEntity<?> getActiveEquipments() {
        return ResponseEntity.ok(storageEquipmentService.getActiveEquipments());
    }
}
