package com.blood.controller.donor;

import com.blood.dto.Donor.DonorHealthRecordResponse;
import com.blood.dto.Donor.DonorHistoryResponse;
import com.blood.dto.Donor.DonorStatResponse;
import com.blood.dto.Donor.HistoryDetailResponse;
import com.blood.dto.Profile.UpdateDonorProfileRequest;
import com.blood.model.Donor;
import com.blood.model.enumformat.EventRegisStatus;
import com.blood.model.Users;
import com.blood.repository.DonorRepository;
import com.blood.repository.EventRegistrationRepository;
import com.blood.repository.UserRepository;
import com.blood.service.DonorService;
import com.blood.service.StatService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@RequestMapping("/api/donor")
public class DonorController {
    private final DonorService donorService;
    private final StatService statService;

    @PutMapping("/update-donor")
    public ResponseEntity<?> updateDonor(@RequestBody UpdateDonorProfileRequest rq) {
            String message = donorService.updateDonorProfile(rq);
            return ResponseEntity.ok().body(message);
    }

    @GetMapping("/donor-stat")
    public ResponseEntity<?> getDonorStat() {
        DonorStatResponse response = statService.getDonorStat();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my-health-record")
    public ResponseEntity<?> getMyHealthRecord() {
        DonorHealthRecordResponse response = donorService.getMyHealthRecord();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my-history")
    public ResponseEntity<?> getMyHistory() {
        DonorHistoryResponse response = donorService.getMyHistory();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/history-detail/{registrationId}")
    public ResponseEntity<?> getHistoryDetail(@PathVariable Integer registrationId) {
        HistoryDetailResponse response = donorService.getHistoryDetail(registrationId);
        return ResponseEntity.ok(response);
    }

}
