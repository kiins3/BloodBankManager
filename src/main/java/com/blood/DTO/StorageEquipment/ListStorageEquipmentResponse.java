package com.blood.DTO.StorageEquipment;

import com.blood.Model.ProductType;
import com.blood.Model.StorageEquipment;
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

    private String status;

    private Integer currentLoad;
}
