package com.blood.dto.Admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BloodInventoryStatResponse {
    private long totalBloodBag;
    private long pendingTestBloodBag;
    private long expiringBloodBag;
    private long expiredBloodBag;
}