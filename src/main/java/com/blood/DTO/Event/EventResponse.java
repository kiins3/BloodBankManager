package com.blood.DTO.Event;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class EventResponse {
    private Integer eventId;

    private String eventName;

    private String location;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private Integer targetAmount;

    private Integer currentAmount;

    private String status;

}
