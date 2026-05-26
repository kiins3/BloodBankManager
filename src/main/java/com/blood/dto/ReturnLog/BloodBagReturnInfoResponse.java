package com.blood.dto.ReturnLog;

import com.blood.model.enumformat.BloodBagStatus;
import com.blood.model.enumformat.ProductType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class BloodBagReturnInfoResponse {
    private Integer bloodBagId;
    private String bagCode;
    private String bloodType;
    private String rhFactor;
    private ProductType productType;
    private Integer volume;
    private LocalDateTime expiredAt;
    private BloodBagStatus currentStatus;

    private boolean isExpired;
    private String warningMessage;
}
