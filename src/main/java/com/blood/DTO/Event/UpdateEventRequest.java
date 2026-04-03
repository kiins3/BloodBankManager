package com.blood.DTO.Event;

import lombok.Data;

@Data
public class UpdateEventRequest {
    private String eventName;

    private Integer targetAmount;

    private String status;
}
