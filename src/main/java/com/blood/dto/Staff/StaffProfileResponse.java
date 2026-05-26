package com.blood.dto.Staff;

import com.blood.model.enumformat.Position;
import com.blood.model.enumformat.UserStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class StaffProfileResponse {
    private Integer staffId;
    private String fullName;
    private String address;
    private String gender;
    private LocalDate dob;
    private String phone;
    private String email;
    private Position position;
    private UserStatus status;
    private String cccd;
}
