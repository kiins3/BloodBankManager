package com.blood.dto.Staff;

import com.blood.model.enumformat.Position;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StaffAvailabilityResponse {
    private Integer staffId;
    private String fullName;
    private Position position;
    private boolean isAvailable;
}
