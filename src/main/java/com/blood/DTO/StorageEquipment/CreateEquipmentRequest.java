package com.blood.DTO.StorageEquipment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateEquipmentRequest {
    private String name;

    private String productType;

    private String standard;

    private Integer maxCapacity;
}
