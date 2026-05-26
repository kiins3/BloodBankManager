package com.blood.dto.BloodRequest;

import com.blood.model.enumformat.ReturnStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReturnRequest {
    private Integer bloodBagId;
    private Integer hospitalId;
    private String reason;
    private ReturnStatus action;
    private Integer equipmentId;
}
