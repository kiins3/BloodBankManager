package com.blood.dto.StorageEquipment;

import com.blood.model.enumformat.EquipmentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateEquipmentRequest {
    private Integer maxCapacity;
    private EquipmentStatus status;
}
