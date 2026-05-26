package com.blood.dto.Admin;

import com.blood.model.enumformat.Position;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AssignmentStaffResponse {
    private Integer eventId;
    private String eventName;
    private List<StaffInfo> assignedStaff;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class StaffInfo {
        private Integer staffId;
        private String fullName;
        private Position position;
    }
}