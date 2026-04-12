package com.blood.Service;

import com.blood.Config.ExcelExporter;
import com.blood.Model.EventRegistration;
import com.blood.Model.ExportLog;
import com.blood.Repository.EventRegistrationRepository;
import com.blood.Repository.ExportLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ReportService {

    @Autowired
    private EventRegistrationRepository registrationRepository;

    @Autowired
    private ExportLogRepository exportLogRepository;

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
