package com.blood.DTO.BloodRequest;

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
public class ListRequestBloodResponse {
    private Integer requestId;
    private String hospitalName;
    private String priority;
    private LocalDate deadlineDate;
    private LocalDateTime requestedDate;
    private String status;
    private List<DetailRequest> detailRequests;
}
