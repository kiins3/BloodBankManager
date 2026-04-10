package com.blood.DTO.StorageEquipment;

import com.blood.Model.ProductType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateEquipmentRequest {
    private String name;

    private ProductType productType;

    private String standard;

    private Integer maxCapacity;
}
