package com.blood.DTO.BloodRequest;

import com.blood.Model.Priority;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotiMess {
    private String title;

    private String message;

    private Priority priority;

    private String hospitalName;

    private LocalDate deadline;
}
