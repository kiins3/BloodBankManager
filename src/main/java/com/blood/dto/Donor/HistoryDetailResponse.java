package com.blood.dto.Donor;

import com.blood.model.enumformat.EventRegisStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HistoryDetailResponse {
    private Integer registrationId;
    private String eventName;
    private LocalDateTime donationDate;
    private String location;
    private Integer expectedVolume;
    private Integer actualVolume;
    private EventRegisStatus status;
    private String rejectionReason;

    private ScreeningInfo screeningInfo;
    private TestResultInfo testResultInfo;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ScreeningInfo {
        private Double weight;
        private Double hemoglobin;
        private String bloodPressure;
        private Integer heartRate;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TestResultInfo {
        private String hiv;
        private String hbv;
        private String hcv;
        private String syphilis;
        private String malaria;
        private String finalConclusion;
        private LocalDateTime resultDate;
    }
}