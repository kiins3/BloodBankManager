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

    @Enumerated(EnumType.STRING)
    @Column (name = "status")
    private UserStatus status;

    @Column (name = "created_at")
    private LocalDateTime createdAt;

    @OneToOne (mappedBy = "user")
    @JsonIgnore
    private Donor donor;

    @OneToOne (mappedBy = "user")
    @JsonIgnore
    private Hospital hospital;

    @OneToOne (mappedBy = "user")
    @JsonIgnore
    private Staff staff;


}
