package com.blood.Controller;

import com.blood.DTO.Blood.ListBloodBagResponse;
import com.blood.DTO.StorageEquipment.ListStorageEquipmentResponse;
import com.blood.Service.StorageEquipmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/storage-equipment")
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
}