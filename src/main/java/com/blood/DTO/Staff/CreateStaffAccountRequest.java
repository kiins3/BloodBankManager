package com.blood.DTO.Staff;

import com.blood.Model.Position;
import com.blood.Model.UserStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateStaffAccountRequest {
    private String fullName;

    private String email;

    private String password;

    private LocalDate dob;

    private String cccd;

    private String gender;

    private String phone;

    private String role;

    private Position position;

    private UserStatus status;
}
