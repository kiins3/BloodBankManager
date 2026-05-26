package com.blood.dto.Hospital;

import com.blood.model.enumformat.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HospitalResponse {
    private Integer hospitalId;
    private String hospitalName;
    private String address;
    private String hotline;
    private String email;
    private UserStatus status;
}
