package com.blood.DTO.Profile;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateDonorProfileRequest {
    private String fullName;

    private String gender;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    private LocalDate dob;

    private String bloodType;

    private String rhFactor;

    private String address;
}
