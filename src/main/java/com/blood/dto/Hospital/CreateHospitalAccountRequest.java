package com.blood.dto.Hospital;

import com.blood.model.enumformat.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateHospitalAccountRequest {
    private String hospitalName;

    private String email;

    private String password;

    private String address;

    private String hotline;

    private UserStatus status;
}
