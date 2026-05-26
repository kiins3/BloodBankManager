package com.blood.controller.staff;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.blood.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/staff/reports")
@SecurityRequirement(name = "bearerAuth")
@Slf4j
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/events/{eventId}/donors")
    public ResponseEntity<byte[]> exportDonorsOfEvent(@PathVariable Integer eventId) {

        byte[] excelContent = reportService.exportEventDonors(eventId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDispositionFormData("attachment", "Danh_Sach_Nguoi_Hien_Mau_SK_" + eventId + ".xlsx");

        return ResponseEntity.ok().headers(headers).body(excelContent);
    }

    @GetMapping("/blood-exports/{exportId}/invoice")
    public ResponseEntity<byte[]> exportBloodInvoice(@PathVariable Integer exportId) {

        byte[] excelContent = reportService.exportBloodInvoice(exportId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDispositionFormData("attachment", "Hoa_Don_Xuat_Kho_" + exportId + ".xlsx");

        return ResponseEntity.ok().headers(headers).body(excelContent);
    }
}