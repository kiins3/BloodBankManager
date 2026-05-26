package com.blood.dto.Admin;

import com.blood.model.enumformat.AssignmentRole;
import com.blood.model.enumformat.EventStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MyAssignmentResponse {
    private List<EventAssignmentInfo> assignments;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class EventAssignmentInfo {
        private Integer eventId;
        private String eventName;
        private String startDate;
        private String endDate;
        private String location;
        private EventStatus eventStatus;
        private AssignmentRole role;
        private long registeredCount;
        private long actualCount;
    }
}