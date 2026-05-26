package com.blood.dto.Staff;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class UpdateStaffForAdminRequest {
    private String fullName;
    private String gender;
    private LocalDate dob;
    private String address;
    private String email;
    private String phone;
}
