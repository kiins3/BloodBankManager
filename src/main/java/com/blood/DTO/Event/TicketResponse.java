package com.blood.DTO.Event;

import com.blood.Model.EventRegisStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketResponse {
    private String eventName;
    private String ticketCode;
    private String qrCode;
    private String location;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String donorName;
    private EventRegisStatus status;
}
