package com.blood.DTO.StorageEquipment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateEquipmentRequest {
    private Integer maxCapacity;
    private String status;
}
