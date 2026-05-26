package com.blood.dto.Staff;

import com.blood.model.enumformat.Position;
import com.blood.model.enumformat.UserStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class StaffDetailResponse {
    private Integer staffId;
    private String fullName;
    private String address;
    private String gender;
    private LocalDate dob;
    private String phone;
    private String email;
    private Position position;
    private UserStatus status;
}
