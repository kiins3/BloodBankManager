package com.blood.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "Donor")
public class Donor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column (name = "donor_id")
    private Integer donorId;

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

    @Column (name = "status")
    private String status;

    @OneToMany (mappedBy = "donor")
    @JsonIgnore
    private List<EventRegistration> registration;
}
