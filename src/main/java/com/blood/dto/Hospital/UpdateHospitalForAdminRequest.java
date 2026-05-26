package com.blood.dto.Hospital;

import com.blood.model.enumformat.UserStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateHospitalForAdminRequest {
    private String hospitalName;
    private String address;
    private String hotline;
    private String email;
}
