package com.blood.dto.StorageEquipment;

import com.blood.model.enumformat.EquipmentStatus;
import com.blood.model.enumformat.ProductType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ListStorageEquipmentResponse {
    private Integer equipmentId;

    private ProductType productType;

    private Integer maxCapacity;

    private String name;

    private String standard;

    private EquipmentStatus status;

    private Integer currentLoad;
}
