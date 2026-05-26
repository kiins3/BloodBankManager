package com.blood.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.blood.config.ExcelExporter;
import com.blood.model.EventRegistration;
import com.blood.model.ExportLog;
import com.blood.repository.EventRegistrationRepository;
import com.blood.repository.ExportLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReportService {

    private final EventRegistrationRepository registrationRepository;
    private final ExportLogRepository exportLogRepository;

    public byte[] exportEventDonors(Integer eventId) {
        List<EventRegistration> registrations = registrationRepository.findByEvents_EventId(eventId);

        if (registrations.isEmpty()) {
            throw new RuntimeException("Không có người hiến máu nào trong sự kiện này!");
        }

        return ExcelExporter.exportDonorListToExcel(registrations);
    }

    public byte[] exportBloodInvoice(Integer exportId) {
        ExportLog exportTicket = exportLogRepository.findById(exportId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu xuất kho"));

        return ExcelExporter.exportIssueInvoiceToExcel(
                exportTicket.getBloodRequest().getHospital().getHospitalName(),
                exportTicket.getExportDate().toString(),
                exportTicket.getManager().getBloodBag()
        );
    }
}
