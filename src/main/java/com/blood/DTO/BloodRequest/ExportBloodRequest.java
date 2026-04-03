package com.blood.DTO.BloodRequest;

import com.blood.Model.BloodBag;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExportBloodRequest {
    private List<Integer> bloodBagId;
}
