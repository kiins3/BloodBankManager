package com.blood.model;

import com.blood.model.enumformat.ReturnStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "return_log")
public class ReturnLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer logId;

    @Column(name = "return_order_id")
    private Integer returnOrderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blood_bag_id", nullable = false)
    private BloodBag bloodBag;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_id", nullable = false)
    private Hospital hospital;

    @Column(name = "reason", length = 255)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_taken", nullable = false)
    private ReturnStatus actionTaken;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id", nullable = true)
    private Staff staff;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}