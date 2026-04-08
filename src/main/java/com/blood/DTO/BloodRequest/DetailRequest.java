package com.blood.DTO.BloodRequest;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DetailRequest {
    private Integer detailId;

    private String bloodType;

    private String productType;

    private Integer volume;

    private Integer quantity;

    private Integer approvedQuantity;
}
