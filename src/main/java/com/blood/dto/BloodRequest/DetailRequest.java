package com.blood.dto.BloodRequest;

import com.blood.model.enumformat.ProductType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

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

    private LocalDate deadline;
}
