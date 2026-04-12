package com.blood.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "Users")
public class Users {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column (name = "user_id")
    private Integer id;

    @Column (name = "email")
    private String email;

    @Column (name = "password")
    private String password;

    @Enumerated(EnumType.STRING)
    @Column (name = "role")
    private Role role;

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
