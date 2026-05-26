package com.blood.dto.BloodRequest;

import com.blood.model.enumformat.ProductType;
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
