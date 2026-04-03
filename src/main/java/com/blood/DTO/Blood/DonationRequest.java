package com.blood.DTO.Blood;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DonationRequest {
    private Integer actualVolume;
    private Boolean isSuccess;
}
