package com.blood.Controller;

import com.blood.DTO.BloodRequest.ExportBloodRequest;
import com.blood.DTO.BloodRequest.ListRequestBloodResponse;
import com.blood.DTO.BloodRequest.RequestBloodRequest;
import com.blood.DTO.BloodRequest.ReviewRequestDTO;
import com.blood.Repository.BloodRequestRepositoty;
import com.blood.Service.BloodRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/blood-request")
public class BloodRequestController {
    @Autowired
    private BloodRequestService bloodRequestService;
    @Autowired
    private BloodRequestRepositoty bloodRequestRepositoty;

    @GetMapping("/list-request")
    public ResponseEntity<?> getBloodRequestList(@RequestParam(required = false) String hospitalName,
                                                 @RequestParam(required = false) String status) {
        try {
            List<ListRequestBloodResponse> response = bloodRequestService.getListRequest(hospitalName, status);
            return ResponseEntity.ok().body(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/my-list-request")
    public ResponseEntity<?> getMyBloodRequestList() {
        try {
            List<ListRequestBloodResponse> responses = bloodRequestService.getMyListRequest();
            return ResponseEntity.ok().body(responses);
        } catch (Exception e)  {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/rq/{hospitalId}")
    public ResponseEntity<?> requestBlood(@PathVariable Integer hospitalId, @RequestBody RequestBloodRequest rq) {
        try {
            String message = bloodRequestService.requestBlood(hospitalId, rq);
            return ResponseEntity.ok().body(message);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/review-request/{requestId}")
    public ResponseEntity<?> reviewBloodRequest(@PathVariable Integer requestId, @RequestBody ReviewRequestDTO rq) {
        try {
            String message = bloodRequestService.reviewRequest(requestId, rq);
            return ResponseEntity.ok().body(message);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/export-blood/{requestId}")
    public ResponseEntity<?> exportBlood(@PathVariable Integer requestId, @RequestBody ExportBloodRequest rq) {
        try {
            String message = bloodRequestService.exportBlood(requestId, rq);
            return ResponseEntity.ok().body(message);
        } catch (Exception e) {
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
