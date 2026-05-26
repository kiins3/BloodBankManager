package com.blood.dto.Event;

import com.blood.model.enumformat.EventRegisStatus;
import com.blood.model.enumformat.EventStatus;
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
    private Integer registrationId;
    private Integer eventId;
    private String eventName;
    private LocalDateTime startDate;
    private EventStatus status;
    private EventRegisStatus registrationStatus;
}
