package com.blood.DTO.Blood;

import com.blood.Model.TestResult;
import com.blood.Model.TestResultValue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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
}
