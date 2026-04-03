package com.blood.DTO.Donor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VisitorRegistRequest {
    private String fullName;
    private String email;
    private String cccd;
    private String gender;
    private LocalDate dob;
    private String phone;
    private String address;
    private String bloodType;
    private String rhFactor;
    private String status;
}
