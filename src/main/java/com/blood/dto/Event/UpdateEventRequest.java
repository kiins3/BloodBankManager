package com.blood.dto.Event;

import com.blood.model.enumformat.EventStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UpdateEventRequest {
    private String eventName;
    private String location;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}
