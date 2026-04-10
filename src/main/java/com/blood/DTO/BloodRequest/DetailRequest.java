package com.blood.DTO.BloodRequest;

import com.blood.Model.ProductType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DetailRequest {
    private Integer detailId;

    private String bloodType;

    private ProductType productType;

    private Integer volume;

    private Integer quantity;

    private Integer approvedQuantity;
}
