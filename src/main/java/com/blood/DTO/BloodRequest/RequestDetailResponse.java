package com.blood.DTO.BloodRequest;

import com.blood.Model.BloodRequestStatus;
import com.blood.Model.Priority;
import com.blood.Model.ProductType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RequestDetailResponse {
    private Integer requestId;
    private String hospitalName;
    private Priority priority;
    private BloodRequestStatus status;
    private LocalDate deadlineDate;
    private LocalDateTime requestedDate;

    private List<DetailRequest> requestedItems;

    private LocalDateTime exportDate;
    private String exportedBy;
    private List<ExportedBagDTO> exportedBags;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ExportedBagDTO {
        private Integer bloodBagId;
        private String bloodType;
        private String rhFactor;
        private ProductType productType;
        private Integer volume;
        private LocalDateTime expiredAt;
        private String storageLocation;
    }
}
