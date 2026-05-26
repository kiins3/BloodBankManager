package com.blood.dto.Admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EventStatusStatResponse {
    private long upcomingEvent;
    private long ongoingEvent;
    private long completedEvent;
}