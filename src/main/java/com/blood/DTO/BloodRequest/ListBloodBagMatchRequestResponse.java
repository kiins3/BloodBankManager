package com.blood.DTO.BloodRequest;

import com.blood.Model.ProductType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ListBloodBagMatchRequestResponse {
    private Integer bloodBagId;
    private ProductType productType;
    private String bloodType;
    private String rhFactor;
    private Integer volume;
    private LocalDateTime expiryDate;
    private String bagCode;
    private String storageLocation;
    private boolean isSuggested;
}
