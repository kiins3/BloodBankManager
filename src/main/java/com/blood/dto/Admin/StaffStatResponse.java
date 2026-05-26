package com.blood.dto.Admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StaffStatResponse {
    private long totalStaff;
    private long techStaff;
    private long inventoryStaff;
    private long activeStaff;
}