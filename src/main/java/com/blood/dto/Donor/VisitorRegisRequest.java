package com.blood.dto.Donor;

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
public class VisitorRegisRequest {
    private String fullName;
    private String email;
    private String cccd;
    private String gender;
    private LocalDate dob;
    private String phone;
    private String address;
    private String bloodType;
    private String rhFactor;
}
