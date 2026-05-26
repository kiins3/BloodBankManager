package com.blood.dto.Hospital;

import com.blood.dto.BloodRequest.ListRequestBloodResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HospitalDashboardStatResponse {
    private long pendingApprovalCount;
    private long inTransitCount;
    private List<ListRequestBloodResponse> recentRequests;
}
