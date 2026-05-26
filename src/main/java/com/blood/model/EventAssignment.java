package com.blood.model;

import com.blood.model.enumformat.AssignmentRole;
import com.blood.model.enumformat.UserStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "event_assignment")
public class EventAssignment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "assignment_id")
    private Integer assignmentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    private Events events;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_id")
    private Staff staff;

    @Column(name = "role")
    private AssignmentRole role;

    @Column(name = "assigned_date")
    private LocalDateTime assignedDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private UserStatus status;
}