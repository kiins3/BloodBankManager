package com.blood.controller.staff;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.blood.dto.Blood.BloodBagDetailResponse;
import com.blood.dto.BloodRequest.*;
import com.blood.model.enumformat.BloodRequestStatus;
import com.blood.repository.BloodRequestRepository;
import com.blood.service.BloodRequestService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/staff/blood-request")
@SecurityRequirement(name = "bearerAuth")
@Slf4j
@RequiredArgsConstructor
public class BloodRequestStaffController {
    private final BloodRequestService bloodRequestService;

    @GetMapping("/list-request")
    public ResponseEntity<?> getBloodRequestList(@RequestParam(required = false) String hospitalName,
                                                 @RequestParam(required = false) BloodRequestStatus status,
                                                 @RequestParam(required = false) String priority){

        List<ListRequestBloodResponse> response = bloodRequestService.getListRequest(hospitalName, status, priority);
        return ResponseEntity.ok().body(response);
    }

    @PatchMapping("/review-request/{requestId}")
    public ResponseEntity<?> reviewBloodRequest(@PathVariable Integer requestId, @RequestBody ReviewRequestDTO rq) {
        String message = bloodRequestService.reviewRequest(requestId, rq);
        return ResponseEntity.ok().body(message);
    }

    @PostMapping("/export-blood/{requestId}")
    public ResponseEntity<?> exportBlood(@PathVariable Integer requestId, @RequestBody ExportBloodRequest rq) {
        String message = bloodRequestService.exportBlood(requestId, rq);
        return ResponseEntity.ok().body(message);
    }

    @GetMapping("/list-suggested-bag/{requestId}")
    public ResponseEntity<?> listSuggestedBags(@PathVariable Integer requestId) {
        return ResponseEntity.ok().body(bloodRequestService.findBagsByBloodRequest(requestId));
    }

    @PostMapping("/scan-blood-bag/{requestId}")
    public ResponseEntity<?> scanBloodBag(@PathVariable Integer requestId, @RequestBody ScanBloodBagRequest rq) {
        BloodBagDetailResponse response = bloodRequestService.scanBloodBag(requestId, rq.getBagCode());
        return ResponseEntity.ok().body(response);
    }

}
