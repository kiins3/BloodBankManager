package com.blood.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table (name = "Hospital")
public class Hospital {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column (name = "hospital_id")
    private Integer hospitalId;

    @JsonIgnore
    @OneToOne (fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "user_id", unique = true)
    private Users user;

    @OneToMany(mappedBy = "hospital")
    private List<BloodRequest> bloodRequests;

    @Column (name = "hospital_name")
    private String hospitalName;

    @Column (name = "address")
    private String address;

    @Column (name = "hotline")
    private String hotline;
}
