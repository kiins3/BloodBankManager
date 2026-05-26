package com.blood.dto.Event;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class VisitorNotiMess {
    private String donorName;
    private String ticketCode;
    private String bloodType;
    private String message;
}
