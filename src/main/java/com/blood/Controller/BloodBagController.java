package com.blood.Controller;

import com.blood.DTO.Blood.*;
import com.blood.Model.BloodBag;
import com.blood.Model.BloodBagStatus;
import com.blood.Model.ProductType;
import com.blood.Service.BloodBagService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/staff/bloodbag")
@SecurityRequirement(name = "bearerAuth")
public class BloodBagController {

    @Autowired
    private BloodBagService bloodBagService;

    @GetMapping("/get-list-blood-bag")
    public ResponseEntity<?> getlistbloodbag(@RequestParam(required = false) Integer bloodBagId,
                                             @RequestParam(required = false) String bloodType,
                                             @RequestParam(required = false) String rhFactor,
                                             @RequestParam(required = false) ProductType productType,
                                             @RequestParam(required = false) BloodBagStatus status) {
        try {
            List<ListBloodBagResponse> list = bloodBagService.getListBloodBag(bloodBagId, bloodType, rhFactor, productType, status);
            return ResponseEntity.ok().body(list);
        } catch (Exception e) {
            return  ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/get-blood-bag-detail/{bloodBagId}")
    public ResponseEntity<?> getbloodbagdetail(@PathVariable Integer bloodBagId) {
        try {
            BloodBagDetailResponse response = bloodBagService.getBloodbagDetails(bloodBagId);
            return ResponseEntity.ok().body(response);
        } catch (RuntimeException e) {
            return  ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/test-result/{bloodBagId}")
    public ResponseEntity<?> testResult(@PathVariable Integer bloodBagId, @RequestBody TestRequest rq, @RequestParam (defaultValue = "false") boolean forceUpdate) {
        try {
            return bloodBagService.testResult(bloodBagId, rq, forceUpdate);
        } catch (Exception e) {
            return  ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/print-label/{bloodBagId}")
    public ResponseEntity<?> printLabel(@PathVariable Integer bloodBagId) {
        try {
            PrintLabelBagResponse response = bloodBagService.printLabelBag(bloodBagId);
            return  ResponseEntity.ok().body(response);
        } catch (RuntimeException e) {
            return  ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/send-mail/{bloodBagId}")
    public ResponseEntity<?> sendEmail(@PathVariable Integer bloodBagId, @RequestParam (defaultValue = "false") boolean forceResend) {
        try {
            return bloodBagService.sendEmail(bloodBagId, forceResend);
        } catch (Exception e) {
            return  ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/separate-blood/{bloodBagId}")
    public ResponseEntity<?> separateBlood(@PathVariable Integer bloodBagId, @RequestBody SeparateBloodRequest rq) {
        try {
            String message = bloodBagService.separateBlood(bloodBagId,rq);
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            return  ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/store-blood/{equipmentId}")
    public ResponseEntity<?> storeBlood(@RequestBody List<Integer> bloodBagId, @PathVariable Integer equipmentId) {
        try {
            String message = bloodBagService.storageBlood(bloodBagId, equipmentId);
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            return  ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/discard-blood")
    public ResponseEntity<?> discardBlood(@RequestBody List<Integer> bloodBagId) {
        try {
            String message = bloodBagService.discardBlood(bloodBagId);
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            return  ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}
