package com.blood.Controller;

import com.blood.DTO.Blood.BloodBagDetailResponse;
import com.blood.DTO.BloodRequest.*;
import com.blood.Repository.BloodRequestRepository;
import com.blood.Service.BloodRequestService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/blood-request")
@SecurityRequirement(name = "bearerAuth")
public class BloodRequestController {
    @Autowired
    private BloodRequestService bloodRequestService;
    @Autowired
    private BloodRequestRepository bloodRequestRepositoty;

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

    @GetMapping("/detail/{requestId}")
    public ResponseEntity<?> getRequestDetail(@PathVariable Integer requestId) {
        try {
            return ResponseEntity.ok().body(bloodRequestService.getRequestDetail(requestId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/rq")
    public ResponseEntity<?> requestBlood(@RequestBody RequestBloodRequest rq) {
        try {
            String message = bloodRequestService.requestBlood(rq);
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


    @GetMapping("/list-suggested-bag/{requestId}")
    public ResponseEntity<?> listSuggestedBags(@PathVariable Integer requestId) {
        try {
            return ResponseEntity.ok().body(bloodRequestService.findBagsByBloodRequest(requestId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/scan-blood-bag/{requestId}")
    public ResponseEntity<?> scanBloodBag(@PathVariable Integer requestId, @RequestBody ScanBloodBagRequest rq) {
        try {
            BloodBagDetailResponse response = bloodRequestService.scanBloodBag(requestId, rq.getBagCode());
            return ResponseEntity.ok().body(response);
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
