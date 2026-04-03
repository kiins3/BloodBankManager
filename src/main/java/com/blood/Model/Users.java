package com.blood.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "Users")
@Data
@AllArgsConstructor
@NoArgsConstructor

public class Users {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column (name = "user_id")
    private Integer id;

    @Column (name = "email")
    private String email;

    @Column (name = "password")
    private String password;

    @Column (name = "role")
    private String role;

    @Column (name = "status")
    private String status;

    @Column (name = "created_at")
    private LocalDateTime createdAt;

    @OneToOne (mappedBy = "user", cascade = CascadeType.ALL)
    @JsonIgnore
    private Donor donor;

    @OneToOne (mappedBy = "user", cascade = CascadeType.ALL)
    @JsonIgnore
    private Hospital hospital;

    @OneToOne (mappedBy = "user", cascade = CascadeType.ALL)
    @JsonIgnore
    private Staff staff;


}
