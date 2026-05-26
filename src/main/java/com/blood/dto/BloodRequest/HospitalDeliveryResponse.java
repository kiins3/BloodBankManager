package com.blood.dto.BloodRequest;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class HospitalDeliveryResponse {
    private List<RejectedBagInfo> rejectedBags = new ArrayList<>();
}



