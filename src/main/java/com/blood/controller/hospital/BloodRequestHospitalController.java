package com.blood.controller.hospital;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.blood.dto.BloodRequest.*;
import com.blood.service.BloodRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@SecurityRequirement(name = "bearerAuth")
@RequestMapping("/api/hospital/blood-request")
@Slf4j
@RequiredArgsConstructor
public class BloodRequestHospitalController {
    private final BloodRequestService bloodRequestService;

    @GetMapping("/my-list-request")
    public ResponseEntity<?> getMyBloodRequestList() {
        List<ListRequestBloodResponse> responses = bloodRequestService.getMyListRequest();
        return ResponseEntity.ok().body(responses);
    }

    @PostMapping("/rq")
    public ResponseEntity<?> requestBlood(@RequestBody RequestBloodRequest rq) {
        String message = bloodRequestService.requestBlood(rq);
        return ResponseEntity.ok().body(message);
    }

    @PutMapping("/update/{requestId}")
    public ResponseEntity<?> updateBloodRequest(@PathVariable Integer requestId, @RequestBody RequestBloodRequest rq) {
        String message = bloodRequestService.updateBloodRequest(requestId, rq);
        return ResponseEntity.ok().body(message);
    }

    @PatchMapping("/cancel/{requestId}")
    public ResponseEntity<?> cancelBloodRequest(@PathVariable Integer requestId) {
        String message = bloodRequestService.cancelBloodRequest(requestId);
        return ResponseEntity.ok().body(message);
    }

    @PatchMapping("/track-order/{requestId}")
    public ResponseEntity<?> confirmDelivery(
            @PathVariable Integer requestId,
            @RequestBody(required = false) HospitalDeliveryResponse response) {

        String result = bloodRequestService.trackOrder(requestId, response);
        return ResponseEntity.ok(result);
    }
}
