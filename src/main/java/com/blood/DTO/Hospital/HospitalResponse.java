package com.blood.DTO.Hospital;

import com.blood.Model.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HospitalResponse {
    private String hospitalName;
    private String address;
    private String hotline;
    private String email;
    private UserStatus status;
}
