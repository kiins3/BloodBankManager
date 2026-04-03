package com.blood.Model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "Test_Result")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TestResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "test_id")
    private Integer testId;

    @OneToOne
    @JoinColumn(name = "blood_bag_id")
    private BloodBag bloodBag;

    @ManyToOne
    @JoinColumn(name = "technician_id")
    private Staff staff;

    @Column(name = "hiv_result")
    private String hiv;

    @Column(name = "hbv_result")
    private String hbv;

    @Column(name = "hcv_result")
    private String hcv;

    @Column(name = "syphilis_result")
    private String syphilis;

    @Column(name = "malaria_result")
    private String malaria;

    @Column(name = "result_date")
    private LocalDateTime resultDate;

    @Column(name = "final_conclusion")
    private String finalConclusion;

    @Column(name = "is_email_sent")
    private boolean isEmailSent =  false;
}
