package com.blood.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table (name = "Events")
public class Events {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column (name = "event_id")
    private Integer eventId;

    @Column (name = "event_name")
    private String eventName;

    @Column (name = "location")
    private String location;

    @Column (name = "start_date")
    private LocalDateTime startDate;

    @Column (name = "end_date")
    private LocalDateTime endDate;

    @Column (name = "target_amount")
    private Integer targetAmount;

    @Column (name = "status")
    private String status;

    @OneToMany (mappedBy = "events", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<EventRegistration> registrations;
}
