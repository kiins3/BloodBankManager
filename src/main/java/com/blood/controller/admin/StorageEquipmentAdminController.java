package com.blood.controller.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.blood.dto.StorageEquipment.CreateEquipmentRequest;
import com.blood.dto.StorageEquipment.UpdateEquipmentRequest;
import com.blood.service.StorageEquipmentService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/storage-equipment")
@SecurityRequirement(name = "bearerAuth")
@Slf4j
@RequiredArgsConstructor
public class StorageEquipmentAdminController {
    private final StorageEquipmentService storageEquipmentService;

    @PostMapping("/create-equipment")
    public ResponseEntity<?> createStorageEquipment(@RequestBody CreateEquipmentRequest rq) {
        String message = storageEquipmentService.createStorageEquipment(rq);
        return ResponseEntity.ok().body(message);
    }

    @PatchMapping("/update-equipment/{equipmentId}")
    public ResponseEntity<?> updateStorageEquipment(@PathVariable Integer equipmentId, @RequestBody UpdateEquipmentRequest rq) {
        String message = storageEquipmentService.updateStorageEquipment(equipmentId, rq);
        return ResponseEntity.ok().body(message);
    }
}