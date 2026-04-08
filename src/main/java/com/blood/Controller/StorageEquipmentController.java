package com.blood.Controller;

import com.blood.DTO.Blood.ListBloodBagResponse;
import com.blood.DTO.StorageEquipment.CreateEquipmentRequest;
import com.blood.DTO.StorageEquipment.ListStorageEquipmentResponse;
import com.blood.DTO.StorageEquipment.UpdateEquipmentRequest;
import com.blood.Model.StorageEquipment;
import com.blood.Service.StorageEquipmentService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/storage-equipment")
@SecurityRequirement(name = "bearerAuth")
public class StorageEquipmentController {
    @Autowired
    private StorageEquipmentService storageEquipmentService;

    @GetMapping("/list-equipment")
    public ResponseEntity<?> getStorageEquipmentList(@RequestParam(required = false) Integer bloodBagId,
                                                     @RequestParam(required = false) String productType) {
        try {
            List<ListStorageEquipmentResponse> list = storageEquipmentService.getListStorageEquipment(bloodBagId, productType);
            return ResponseEntity.ok().body(list);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/create-equipment")
    public ResponseEntity<?> createStorageEquipment(@RequestBody CreateEquipmentRequest rq) {
        try {
            String message = storageEquipmentService.createStorageEquipment(rq);
            return ResponseEntity.ok().body(message);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/update-equipment/{equipmentId}")
    public ResponseEntity<?> updateStorageEquipment(@PathVariable Integer equipmentId, @RequestBody UpdateEquipmentRequest rq) {
        try {
            String message = storageEquipmentService.updateStorageEquipment(equipmentId, rq);
            return ResponseEntity.ok().body(message);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}