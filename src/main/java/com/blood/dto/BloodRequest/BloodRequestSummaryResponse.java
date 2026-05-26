package com.blood.dto.BloodRequest;

import com.blood.model.enumformat.BloodRequestStatus;
import com.blood.model.enumformat.Priority;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class BloodRequestSummaryResponse {
    private Integer requestId;
    private Priority priority;
    private LocalDate deadlineDate;
    private LocalDateTime requestedDate;
    private BloodRequestStatus status;
    private Integer totalQuantity;
}