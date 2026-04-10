package com.blood.DTO.Event;

import com.blood.Model.EventStatus;
import lombok.Data;

@Data
public class UpdateEventRequest {
    private String eventName;

    private Integer targetAmount;

    private EventStatus status;
}
