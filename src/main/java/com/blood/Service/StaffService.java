package com.blood.Service;

import com.blood.DTO.Staff.StaffResponse;
import com.blood.Model.Staff;
import com.blood.Repository.StaffRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class StaffService {

    @Autowired
    private StaffRepository staffRepository;

    public List<StaffResponse> getAllStaff() {
        List<Staff> staffList = staffRepository.findAll();
        return staffList.stream().map(staff ->
            StaffResponse.builder()
                    .fullName(staff.getFullName())
                    .cccd(staff.getCccd())
                    .gender(staff.getGender())
                    .dob(staff.getDob())
                    .phone(staff.getPhone())
                    .email(staff.getUser().getEmail())
                    .position(staff.getPosition())
                    .status(staff.getStatus())
                    .build())
            .collect(Collectors.toList());
    }
}
