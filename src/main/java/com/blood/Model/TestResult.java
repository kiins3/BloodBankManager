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

    @ManyToOne (fetch = FetchType.LAZY)
    @JoinColumn(name = "technician_id")
    private Staff staff;

    @Enumerated(EnumType.STRING)
    @Column(name = "hiv_result")
    private TestResultValue hiv;

    @Enumerated(EnumType.STRING)
    @Column(name = "hbv_result")
    private TestResultValue hbv;

    @Enumerated(EnumType.STRING)
    @Column(name = "hcv_result")
    private TestResultValue hcv;

    @Enumerated(EnumType.STRING)
    @Column(name = "syphilis_result")
    private TestResultValue syphilis;

    @Enumerated(EnumType.STRING)
    @Column(name = "malaria_result")
    private TestResultValue malaria;

    @Column(name = "result_date")
    private LocalDateTime resultDate;

    @Column(name = "final_conclusion")
    private String finalConclusion;

    @Column(name = "is_email_sent")
    private boolean isEmailSent =  false;
}
