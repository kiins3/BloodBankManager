package com.blood.model;

import com.blood.model.enumformat.Position;
import com.blood.model.enumformat.UserStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table (name = "staff")
public class Staff {
    @Id
    @Column(name = "staff_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer staffId;

    @JsonIgnore
    @OneToOne (fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "user_id",unique = true)
    private Users user;

    @OneToMany(mappedBy = "staff")
    private List<TestResult> testResults;

    @OneToMany(mappedBy = "staff")
    private List<BloodBag> bloodBag;

    @OneToMany(mappedBy = "manager")
    private List<ExportLog> exportLogs;

    @JsonIgnore
    @OneToMany(mappedBy = "staff", fetch = FetchType.LAZY)
    private List<ActionLog> actionLogs;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "dob")
    private LocalDate dob;

    @Column(name = "cccd")
    private String cccd;

    @Column(name = "gender")
    private String gender;

    @Column(name = "phone")
    private String phone;

    @Column(name = "address")
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(name = "position")
    private Position position;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private UserStatus status;
}
