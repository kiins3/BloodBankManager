package com.blood.DTO.Event;


import com.blood.Model.EventRegisStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RegistrationResponse {
    private String qrCode;

    private LocalDateTime createdAt;

    private EventRegisStatus status;
}
