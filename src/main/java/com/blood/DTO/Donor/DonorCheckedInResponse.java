package com.blood.DTO.Donor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DonorCheckedInResponse {
    private String fullName;

    private String gender;

    private LocalDate dob;

    private String phone;

    private String address;
}
