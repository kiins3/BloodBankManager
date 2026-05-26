package com.blood.dto.Blood;

import com.blood.model.enumformat.ProductType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PrintLabelBagResponse {
    private String bagCode;
    private String barCodeBag;
    private String bloodType;
    private String rhFactor;
    private ProductType productType;
    private LocalDateTime expiryDate;
    private Integer volume;
}
