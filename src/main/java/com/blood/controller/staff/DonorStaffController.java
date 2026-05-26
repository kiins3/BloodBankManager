package com.blood.controller.staff;

import com.blood.dto.Donor.*;
import com.blood.service.RegistrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.blood.model.enumformat.EventRegisStatus;
import com.blood.model.enumformat.UserStatus;
import com.blood.service.DonorService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/staff/donor")
@SecurityRequirement(name = "bearerAuth")
@Slf4j
@RequiredArgsConstructor
public class DonorStaffController {
    private final DonorService donorService;
    private final RegistrationService registrationService;

    @GetMapping("/get-list-donor")
    public ResponseEntity<Page<GetListDonorResponse>> getListDonor(@RequestParam(required = false) String keyword,
                                                   @RequestParam(required = false) String bloodType,
                                                   @RequestParam(required = false) String rhFactor,
                                                   @RequestParam(required = false) UserStatus status,
                                                   @PageableDefault(page = 0, size = 10) Pageable pageable){
        Page<GetListDonorResponse> result = donorService.getAllDonors(keyword, bloodType, rhFactor, status, pageable);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/get-list-donor-status/{eventId}")
    public List<DonorCheckedInResponse> getListDonor(@PathVariable Integer eventId, @RequestParam(required = false) EventRegisStatus status){
        return donorService.getListDonorsByStatus(eventId, status);
    }

    @GetMapping("/donor-detail/{donorId}")
    public ResponseEntity<DonorDetailResponse> getDetail(@PathVariable Integer donorId) {
        return ResponseEntity.ok(donorService.getDonorDetail(donorId));
    }

    @PatchMapping("/update-donor/{donorId}")
    public ResponseEntity<?> updateDonorForAdmin(@PathVariable Integer donorId, @RequestBody UpdateDonorForAdminRequest rq) {
        String response = donorService.updateDonorForAdmin(donorId, rq);
        return  ResponseEntity.ok(response);
    }

    @GetMapping("/history-donate/{donorId}")
    public ResponseEntity<Page<DonorHistoryResponse.DonationHistory>> getHistory(
            @PathVariable Integer donorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(donorService.getDonorHistoryForAdmin(donorId, pageable));
    }

    @PostMapping("/regis-for-visitor/{eventId}")
    public ResponseEntity<?> regisForVisitor(@PathVariable Integer eventId, @RequestBody VisitorRegisRequest rq){
        String message = registrationService.regisForVisitor(eventId, rq);
        return ResponseEntity.ok().body(message);
    }

    @PostMapping("/call-for-blood")
    public ResponseEntity<?> callForBlood(@RequestParam String bloodType,
                                          @RequestParam String rhFactor){
        String message = donorService.callForBloodDonation(bloodType, rhFactor);
        return ResponseEntity.ok().body(message);
    }

    @PatchMapping("/cancel-visitor/{registrationId}")
    public ResponseEntity<?> cancelVisitorRegistration(@PathVariable Integer registrationId){
        log.info("Cancel visitor registration request for registrationId: {}", registrationId);
        String message = registrationService.cancelVisitorRegistration(registrationId);
        log.info("Visitor registration {} cancelled successfully", registrationId);
        return ResponseEntity.ok().body(message);
    }
}
