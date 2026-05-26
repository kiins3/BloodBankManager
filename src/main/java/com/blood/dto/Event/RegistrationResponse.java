package com.blood.dto.Event;


import com.blood.model.enumformat.EventRegisStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RegistrationResponse {
    private String qrCode;

    private LocalDateTime createdAt;

    private EventRegisStatus status;
}
