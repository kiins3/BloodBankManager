package com.blood.dto.Staff;

import com.blood.model.enumformat.Position;
import com.blood.model.enumformat.Role;
import com.blood.model.enumformat.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateStaffAccountRequest {
    private String fullName;
    private String email;
    private LocalDate dob;
    private String cccd;
    private String gender;
    private String phone;
    private Role role;
    private Position position;
}
