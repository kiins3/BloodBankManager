package com.blood.DTO.Profile;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetHospitalProfileResponse {
    private String email;

    private String hospitalName;

    private String hotline;

    private String address;
}
