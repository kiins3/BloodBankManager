package com.blood.dto.Donor;

import com.blood.model.enumformat.EventRegisStatus;
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

    private EventRegisStatus status;

    private LocalDateTime lastDonationDate;
}
