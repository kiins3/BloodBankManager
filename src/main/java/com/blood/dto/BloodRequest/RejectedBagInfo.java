package com.blood.dto.BloodRequest;

import lombok.Data;

@Data
public class RejectedBagInfo {
    private Integer bloodBagId;
    private String reason;
}