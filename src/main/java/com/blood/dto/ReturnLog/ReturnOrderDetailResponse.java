package com.blood.dto.ReturnLog;

import com.blood.model.enumformat.BloodBagStatus;
import com.blood.model.enumformat.ProductType;
import com.blood.model.enumformat.ReturnStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ReturnOrderDetailResponse {
    private Integer returnOrderId;
    private String hospitalName;
    private LocalDateTime createdAt;
    private String processedBy;

    private List<BagItem> bags;

    private int totalCount;
    private int pendingCount;
    private boolean allProcessed;

    @Data
    public static class BagItem {
        private Integer logId;
        private Integer bloodBagId;
        private String bagCode;
        private String bloodType;
        private String rhFactor;
        private ProductType productType;
        private Integer volume;
        private LocalDateTime expiredAt;
        private BloodBagStatus currentStatus;
        private boolean expired;
        private ReturnStatus actionTaken;
        private String reason;
    }
}
