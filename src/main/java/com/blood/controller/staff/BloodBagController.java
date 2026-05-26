package com.blood.controller.staff;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.blood.dto.Blood.*;
import com.blood.model.enumformat.BloodBagStatus;
import com.blood.model.enumformat.ProductType;
import com.blood.service.BloodBagService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/staff/bloodbag")
@SecurityRequirement(name = "bearerAuth")
@Slf4j
@RequiredArgsConstructor
public class BloodBagController {

    private final BloodBagService bloodBagService;

    @GetMapping("/get-list-blood-bag")
    public ResponseEntity<?> getListBloodBag(@RequestParam(required = false) Integer bloodBagId,
                                             @RequestParam(required = false) String bloodType,
                                             @RequestParam(required = false) String rhFactor,
                                             @RequestParam(required = false) ProductType productType,
                                             @RequestParam(required = false) BloodBagStatus status) {
        List<ListBloodBagResponse> list = bloodBagService.getListBloodBag(bloodBagId, bloodType, rhFactor, productType, status);
        return ResponseEntity.ok().body(list);
    }

    @GetMapping("/get-blood-bag-detail/{bloodBagId}")
    public ResponseEntity<?> getBloodBagDetail(@PathVariable Integer bloodBagId) {
        BloodBagDetailResponse response = bloodBagService.getBloodbagDetails(bloodBagId);
        return ResponseEntity.ok().body(response);
    }

    @PostMapping("/test-result/{bloodBagId}")
    public ResponseEntity<?> testResult(@PathVariable Integer bloodBagId, @RequestBody TestRequest rq, @RequestParam (defaultValue = "false") boolean forceUpdate) {
        return bloodBagService.testResult(bloodBagId, rq, forceUpdate);
    }

    @PostMapping("/print-label/{bloodBagId}")
    public ResponseEntity<?> printLabel(@PathVariable Integer bloodBagId) {
        PrintLabelBagResponse response = bloodBagService.printLabelBag(bloodBagId);
        return  ResponseEntity.ok().body(response);
    }

    @PostMapping("/send-mail/{bloodBagId}")
    public ResponseEntity<?> sendEmail(@PathVariable Integer bloodBagId, @RequestParam (defaultValue = "false") boolean forceResend) {
        return bloodBagService.sendEmail(bloodBagId, forceResend);
    }

    @PostMapping("/separate-blood/{bloodBagId}")
    public ResponseEntity<?> separateBlood(@PathVariable Integer bloodBagId, @RequestBody SeparateBloodRequest rq) {
        String message = bloodBagService.separateBlood(bloodBagId,rq);
        return ResponseEntity.ok(message);
    }

    @PostMapping("/store-blood/{equipmentId}")
    public ResponseEntity<?> storeBlood(@RequestBody List<Integer> bloodBagIds, @PathVariable Integer equipmentId) {
        String message = bloodBagService.storageBlood(bloodBagIds, equipmentId);
        return ResponseEntity.ok(message);
    }

    @PatchMapping("/transfer-storage/{equipmentId}")
    public ResponseEntity<?> transferStorage(@RequestBody List<Integer> bloodBagIds, @PathVariable Integer equipmentId) {
        String message = bloodBagService.transferStorage(bloodBagIds, equipmentId);
        return ResponseEntity.ok(message);
    }

    @PatchMapping("/discard-blood")
    public ResponseEntity<?> discardBlood(@RequestBody List<Integer> bloodBagIds) {
        String message = bloodBagService.discardBlood(bloodBagIds);
        return ResponseEntity.ok(message);
    }

    @PatchMapping("/undo-discard-blood")
    public ResponseEntity<?> undoDiscardBlood(@RequestBody List<Integer> bloodBagIds) {
        String message = bloodBagService.undoDiscardBlood(bloodBagIds);
        return ResponseEntity.ok(message);
    }

}
