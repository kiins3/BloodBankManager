package com.blood.dto.Staff;

import com.blood.model.enumformat.Position;
import com.blood.model.enumformat.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StaffResponse {
    private Integer staffId;
    private String fullName;
    private String phone;
    private String email;
    private Position position;
    private UserStatus status;
}
