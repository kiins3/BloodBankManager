package com.blood.Model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "Event_Registration")
public class EventRegistration {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer registrationId;

    @ManyToOne
    @JoinColumn (name = "event_id")
    private Events events;

    @ManyToOne
    @JoinColumn (name = "donor_id")
    private Donor donor;

    @OneToMany (mappedBy = "registration")
    private List<BloodBag> bloodBag;

    @Column(name = "ticket_code",  unique = true)
    private String ticketCode;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "weight")
    private BigDecimal weight;

    @Column(name = "hemoglobin")
    private BigDecimal hemoglobin;

    @Column(name = "blood_pressure")
    private String bloodPressure;

    @Column(name = "heart_rate")
    private Integer heartRate;

    @Column(name = "actual_volume")
    private Integer actualVolume;

    @Column(name = "expected_volume")
    private Integer expectedVolume;

    @Column(name = "status")
    private String status;

    @Column(name = "rejection_reason")
    private String rejectionReason;
}
