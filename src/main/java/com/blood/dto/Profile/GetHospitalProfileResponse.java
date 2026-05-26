package com.blood.dto.Profile;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetHospitalProfileResponse {
    private Integer hospitalId;

    private String email;

    private String hospitalName;

    private String hotline;

    private String address;
}
