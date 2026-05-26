package com.blood.dto.Blood;

import com.blood.model.enumformat.BloodBagStatus;
import com.blood.model.enumformat.ProductType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ListBloodBagResponse {
    private Integer bloodBagId;
    private LocalDateTime collectedAt;
    private String bloodType;
    private String rhFactor;
    private String bloodFactor;
    private ProductType productType;
    private String storageLocation;
    private BloodBagStatus status;
}
