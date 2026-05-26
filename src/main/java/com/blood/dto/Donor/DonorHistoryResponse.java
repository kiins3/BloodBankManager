package com.blood.dto.Donor;

import com.blood.model.enumformat.EventRegisStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DonorHistoryResponse {
    private List<DonationHistory> history;

    @Data
    @Builder
    public static class DonationHistory {
        private Integer registrationId;
        private LocalDateTime donationDate;
        private String location;
        private Integer volumeMl;
        private EventRegisStatus status;
        private String eventName;
    }
}