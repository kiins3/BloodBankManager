package com.blood.dto.Admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EventStatResponse {
    private Integer eventId;
    private String eventName;
    private long registeredCount;
    private long actualCount;
}