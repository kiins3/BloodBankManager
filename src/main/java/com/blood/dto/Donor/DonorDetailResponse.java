package com.blood.dto.Donor;

import com.blood.model.enumformat.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DonorDetailResponse {
    private Integer donorId;
    private String fullName;
    private String phone;
    private String email;
    private String bloodType;
    private String rhFactor;
    private UserStatus userStatus;

    private String dob;
    private String gender;
    private String address;
    private boolean hasUnsafeTest;
    private String displayStatus;

    private Long totalDonations;
    private LocalDateTime lastDonationDate;


}
