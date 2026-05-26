package com.blood.dto.Donor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DonorStatResponse {
    private String bloodType;
    private String rhFactor;
    private long totalTimeDonate;
}