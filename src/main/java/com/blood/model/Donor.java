package com.blood.model;

import com.blood.model.enumformat.UserStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "donor")
public class Donor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column (name = "donor_id")
    private Integer donorId;

    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "user_id", referencedColumnName = "user_id", unique = true)
    private Users user;

    @Column (name = "full_name")
    private String fullName;

    @Column (name = "cccd")
    private String cccd;

    @Column (name = "email")
    private String email;

    @Column (name = "phone")
    private String phone;

    @Column (name = "blood_type")
    private String bloodType;

    @Column (name = "rh_factor")
    private String rhFactor;

    @Column (name = "gender")
    private String gender;

    @Column (name = "dob")
    private LocalDate dob;

    @Column (name = "address")
    private String address;

    @Enumerated(EnumType.STRING)
    @Column (name = "status")
    private UserStatus status;

    @Column(name = "block_booking_until")
    private LocalDateTime blockBookingUntil;

    @OneToMany (mappedBy = "donor")
    @JsonIgnore
    private List<EventRegistration> registration;
}
