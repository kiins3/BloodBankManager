package com.blood.DTO.Donor;

import com.blood.Model.UserStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class GetListDonorResponse {
    private Integer donorId;

    private String fullName;

    private String phone;

    private String email;

    private String bloodType;

    private String rhFactor;

    private UserStatus userStatus;

    private Long totalDonations;

    private LocalDateTime lastDonationDate;

    private String displayStatus;

    public GetListDonorResponse(Integer donorId, String fullName,String phone, String email,
                                String bloodType, String rhFactor, UserStatus userStatus,
                                Long totalDonations, LocalDateTime lastDonationDate) {
        this.donorId = donorId;
        this.fullName = fullName;
        this.phone = phone;
        this.email = email;
        this.bloodType = bloodType;
        this.rhFactor = rhFactor;
        this.userStatus = userStatus;
        this.totalDonations = totalDonations;
        this.lastDonationDate = lastDonationDate;
    }

}
