package com.blood.dto.Event;

import lombok.*;

@Data
@AllArgsConstructor
public class NotiMessDonorCheckIn {
    private String donorName;
    private String ticketCode;
    private String message;
}
