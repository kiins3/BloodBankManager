package com.blood.dto.Admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StorageEquipmentStatResponse {
    private long totalEquipment;
    private long activeEquipment;
    private long nearlyFullEquipment;
    private long maintenanceEquipment;
}