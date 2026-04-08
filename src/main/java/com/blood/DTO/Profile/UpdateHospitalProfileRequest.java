package com.blood.DTO.Profile;

import lombok.Data;

@Data
public class UpdateHospitalProfileRequest {
    private String hospitalName;

    private String address;

    private String hotline;
}
