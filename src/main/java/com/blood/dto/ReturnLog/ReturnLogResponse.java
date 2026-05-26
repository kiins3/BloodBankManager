package com.blood.dto.ReturnLog;

import com.blood.model.enumformat.BloodBagStatus;
import com.blood.model.enumformat.ProductType;
import com.blood.model.enumformat.ReturnStatus;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ReturnLogResponse {
    private Integer logId;
    private Integer bloodBagId;
    private ProductType productType;
    private String bloodType;
    private String bagCode;
    private String hospitalName;
    private String reason;
    private ReturnStatus actionTaken;
    private String staffName;
    private LocalDateTime createdAt;
    private BloodBagStatus currentBagStatus;
}