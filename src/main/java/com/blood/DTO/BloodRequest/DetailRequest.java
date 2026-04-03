package com.blood.DTO.BloodRequest;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DetailRequest {
    private String bloodType;

    private String productType;

    private Integer volume;

    private Integer quantity;
}
