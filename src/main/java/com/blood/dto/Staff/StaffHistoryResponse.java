package com.blood.dto.Staff;

import com.blood.model.enumformat.AssignmentRole;
import com.blood.model.enumformat.UserStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class StaffHistoryResponse {
    private Integer eventId;
    private String eventName;
    private LocalDateTime startDate;
    private String location;
    private AssignmentRole role;
    private UserStatus assignmentStatus;
}