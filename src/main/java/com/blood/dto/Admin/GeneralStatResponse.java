package com.blood.dto.Admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GeneralStatResponse {
    private long totalBloodBag;
    private long totalEvent;
    private long totalRequest;
    private long totalExpiringBloodBag;
}
