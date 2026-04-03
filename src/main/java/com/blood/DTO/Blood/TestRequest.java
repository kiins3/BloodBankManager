package com.blood.DTO.Blood;

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

    private String hiv;

    private String hbv;

    private String hcv;

    private String syphilis;

    private String malaria;
}
