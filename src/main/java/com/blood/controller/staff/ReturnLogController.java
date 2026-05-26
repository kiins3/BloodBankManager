package com.blood.controller.staff;

import com.blood.dto.ReturnLog.*;
import com.blood.model.enumformat.ReturnStatus;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.extern.slf4j.Slf4j;

import com.blood.dto.BloodRequest.ReturnRequest;
import com.blood.service.ReturnLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/staff/return-log")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
@Slf4j
public class ReturnLogController {
    private final ReturnLogService returnLogService;

    @GetMapping("/list-return")
    public ResponseEntity<Page<ReturnLogResponse>> getAllReturns(
            @RequestParam(required = false) String hospitalName,
            @RequestParam(required = false) ReturnStatus action,
            Pageable pageable
    ) {
        return ResponseEntity.ok(returnLogService.getAllReturns(hospitalName, action, pageable));
    }

    @GetMapping("/return-detail/{returnOrderId}")
    public ResponseEntity<ReturnOrderDetailResponse> getReturnOrderDetail(@PathVariable Integer returnOrderId) {
        return ResponseEntity.ok(returnLogService.getReturnOrderDetail(returnOrderId));
    }

    @GetMapping("/scan/{bagCode}")
    public ResponseEntity<BloodBagReturnInfoResponse> scanBagForReturn(@PathVariable String bagCode) {
        return ResponseEntity.ok(returnLogService.scanBagForReturn(bagCode));
    }

    @PostMapping("/inspect")
    public ResponseEntity<BulkInspectResultResponse> processBulkInspection(
            @RequestHeader("X-Staff-Id") Integer processorId,
            @RequestBody BulkInspectRequest request) {
        return ResponseEntity.ok(returnLogService.processBulkInspection(processorId, request));
    }
}
