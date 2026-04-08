package com.blood.DTO.Staff;

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
    private String fullName;
    private String cccd;
    private String gender;
    private LocalDate dob;
    private String phone;
    private String email;
    private String position;
    private String status;
}
