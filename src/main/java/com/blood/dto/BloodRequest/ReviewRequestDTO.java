package com.blood.dto.BloodRequest;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReviewRequestDTO {
    private List<ReviewDetailDTO> approvedDetails;
}
