package com.blood.dto.ReturnLog;

import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public class NotiMessReturnOrder {
    private Integer returnOrderId;
    private int pendingKiemDinhCount;
    private int pendingHuyCount;
    private String message;
}
