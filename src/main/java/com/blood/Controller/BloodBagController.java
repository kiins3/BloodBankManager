package com.blood.Controller;

import com.blood.DTO.Blood.ListBloodBagResponse;
import com.blood.DTO.Blood.SeparateBloodRequest;
import com.blood.DTO.Blood.TestRequest;
import com.blood.Model.BloodBag;
import com.blood.Service.BloodBagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bloodbag")
public class BloodBagController {

    @Autowired
    private BloodBagService bloodBagService;

    @GetMapping("/get-list-blood-bag")
    public ResponseEntity<?> getlistbloodbag(@RequestParam(required = false) Integer bloodBagId,
                                             @RequestParam(required = false) String bloodType,
                                             @RequestParam(required = false) String rhFactor,
                                             @RequestParam(required = false) String productType,
                                             @RequestParam(required = false) String status) {
        try {
            List<ListBloodBagResponse> list = bloodBagService.getListBloodBag(bloodBagId, bloodType, rhFactor, productType, status);
            return ResponseEntity.ok().body(list);
        } catch (Exception e) {
            return  ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/test-result/{bloodBagId}")
    public ResponseEntity<?> testResult(@PathVariable Integer bloodBagId, @RequestBody TestRequest rq) {
        try {
            String message = bloodBagService.testResult(bloodBagId, rq);
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            return  ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/send-mail/{test_id}")
    public ResponseEntity<?> sendEmail(@PathVariable Integer test_id, @RequestParam (defaultValue = "false") boolean forceResend) {
        try {
            return bloodBagService.sendEmail(test_id, forceResend);
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
