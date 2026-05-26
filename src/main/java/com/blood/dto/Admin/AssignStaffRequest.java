package com.blood.dto.Admin;

import com.blood.model.enumformat.AssignmentRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AssignStaffRequest {
    private List<Integer> staffId;
    private AssignmentRole role;
}