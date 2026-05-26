package com.blood.dto.actionlog;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActionLogResponse {
    private Integer logId;

    // Thông tin Staff (null nếu ADMIN thực hiện)
    private Integer staffId;
    private String staffName;

    // Thông tin người thực hiện (luôn có)
    private Integer performedByUserId;
    private String performedByRole;   // ADMIN / STAFF_TECH / STAFF_INVENTORY

    private String actionType;
    private String entityName;
    private String entityId;
    private String oldData;
    private String newData;
    private String note;
    private String status;
    private LocalDateTime createdAt;
}