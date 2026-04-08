package com.blood.DTO.Event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TicketSummaryResponse {
    private Integer eventId;
    private String eventName;
    private LocalDateTime startDate;
    private String status;
}
