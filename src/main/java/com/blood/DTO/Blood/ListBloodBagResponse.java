package com.blood.DTO.Blood;

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
    private String productType;
    private String storageLocation;
    private String status;
}
