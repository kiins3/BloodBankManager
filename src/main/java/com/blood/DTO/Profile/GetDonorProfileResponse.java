package com.blood.DTO.Profile;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetDonorProfileResponse {
    private String email;

    private String fullName;

    private String cccd;

    private String gender;

    private LocalDate dob;

    private String bloodType;

    private String rhFactor;

    private String phone;

    private String address;

    private String status;
}
