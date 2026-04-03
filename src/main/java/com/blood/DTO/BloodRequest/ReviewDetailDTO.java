package com.blood.DTO.BloodRequest;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReviewDetailDTO {
    private Integer detailId;

    private Integer approvedQuantity;
}
