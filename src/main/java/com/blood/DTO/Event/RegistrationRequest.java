package com.blood.DTO.Event;

import lombok.Data;

@Data
public class RegistrationRequest {
    private Integer eventId;

    private Integer donorId;

    private String status;
}
