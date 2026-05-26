package com.blood.dto.Blood;

import com.blood.model.enumformat.TestResultValue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestRequest {
    private String bloodType;

    private String rhFactor;

    private TestResultValue hiv;

    private TestResultValue hbv;

    private TestResultValue hcv;

    private TestResultValue syphilis;

    private TestResultValue malaria;

    private Boolean isPhysicalError;
}
