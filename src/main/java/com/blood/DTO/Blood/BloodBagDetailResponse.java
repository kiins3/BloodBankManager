package com.blood.DTO.Blood;

import com.blood.Model.BloodBagStatus;
import com.blood.Model.ProductType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BloodBagDetailResponse {
    private String donorName;
    private LocalDateTime collectedAt;
    private LocalDateTime expirationDate;
    private String bloodType;
    private String rhFactor;
    private Integer actualVolume;
    private String hiv;
    private String hbv;
    private String hcv;
    private String syphilis;
    private String malaria;
    private String finalConclusion;

    @JsonProperty("storageLocation")
    private String storageLocation;

    @JsonProperty("status")
    private BloodBagStatus status;

    @JsonProperty("productType")
    private ProductType productType;
}
