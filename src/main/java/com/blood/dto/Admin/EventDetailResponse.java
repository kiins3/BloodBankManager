package com.blood.dto.Admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EventDetailResponse {
    private Integer eventId;
    private String eventName;
    private String location;
    private String startDate;
    private String endDate;
    private String status;
    private Integer targetAmount;
    private long registeredCount;
    private long actualCount;
}