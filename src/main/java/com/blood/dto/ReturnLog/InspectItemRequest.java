package com.blood.dto.ReturnLog;

import com.blood.model.enumformat.ReturnStatus;

import lombok.Data;

@Data
public class InspectItemRequest {
    private Integer bloodBagId;

    private ReturnStatus action;

    private String reason;
}
