package com.blood.DTO.Event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScreeningRequest {
    private Integer donorId;

    private BigDecimal weight;

    private BigDecimal hemoglobin;

    private String bloodPressure;

    private Integer heartRate;

    private Integer expectedVolume;

    private Boolean isEligible;

    private String rejectionReason;
}
