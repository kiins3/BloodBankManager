package com.blood.DTO.Donor;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DonorResponse {
    private Integer regisId;

    private Integer donorId;

    private String ticketCode;

    private String fullName;

    private String gender;

    private LocalDate dob;

    private String phone;

    private String address;

    private String status;

    private LocalDateTime lastDonationDate;
}
