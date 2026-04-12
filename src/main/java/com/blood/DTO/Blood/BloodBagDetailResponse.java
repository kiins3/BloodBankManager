package com.blood.DTO.Blood;

import com.blood.Model.BloodBagStatus;
import com.blood.Model.ProductType;
import com.blood.Model.TestResultValue;
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
    private String bagCode;
    private String barcodeBase64;
    private TestResultValue hiv;
    private TestResultValue hbv;
    private TestResultValue hcv;
    private TestResultValue syphilis;
    private TestResultValue malaria;
    private String finalConclusion;

    @JsonProperty("storageLocation")
    private String storageLocation;

    @JsonProperty("status")
    private BloodBagStatus status;

    @JsonProperty("productType")
    private ProductType productType;
}
