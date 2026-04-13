package com.blood.Controller;

import com.blood.DTO.Blood.BloodBagDetailResponse;
import com.blood.DTO.BloodRequest.*;
import com.blood.Repository.BloodRequestRepository;
import com.blood.Service.BloodRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hospital/blood-request")
public class BloodRequestHospitalController {
    @Autowired
    private BloodRequestService bloodRequestService;

    @GetMapping("/my-list-request")
    public ResponseEntity<?> getMyBloodRequestList() {
        try {
            List<ListRequestBloodResponse> responses = bloodRequestService.getMyListRequest();
            return ResponseEntity.ok().body(responses);
        } catch (Exception e)  {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/rq")
    public ResponseEntity<?> requestBlood(@RequestBody RequestBloodRequest rq) {
        try {
            String message = bloodRequestService.requestBlood(rq);
            return ResponseEntity.ok().body(message);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/track-order/{requestId}")
    public ResponseEntity<?> trackOrder(@PathVariable Integer requestId) {
        try {
            String message = bloodRequestService.trackOrder(requestId);
            return ResponseEntity.ok().body(message);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
