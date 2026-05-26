package com.blood.dto.Profile;

import lombok.Data;

@Data
public class UpdateHospitalProfileRequest {
    private String hospitalName;

    private String address;

    private String hotline;
}
