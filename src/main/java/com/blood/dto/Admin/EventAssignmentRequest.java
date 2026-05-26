package com.blood.dto.Admin;

import com.blood.model.enumformat.AssignmentRole;
import lombok.Data;
import java.util.List;

@Data
public class EventAssignmentRequest {
    private Integer eventId;
    private List<StaffRoleDetail> assignments;

    @Data
    public static class StaffRoleDetail {
        private Integer staffId;
        private AssignmentRole role;
    }
}