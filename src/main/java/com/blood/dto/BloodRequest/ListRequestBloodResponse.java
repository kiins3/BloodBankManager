package com.blood.dto.BloodRequest;

import com.blood.model.enumformat.BloodRequestStatus;
import com.blood.model.enumformat.Priority;
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
    private Priority priority;
    private LocalDate deadlineDate;
    private LocalDateTime requestedDate;
    private BloodRequestStatus status;
    private Integer hospitalId;
    private List<DetailRequest> detailRequests;
}

