package com.blood.Model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "Blood_Request")
public class BloodRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "request_id")
    private Integer requestId;

    @ManyToOne (fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_id")
    private Hospital hospital;

    @OneToMany(mappedBy = "bloodRequest",  cascade = CascadeType.ALL)
    private List<RequestDetail> requestDetails;

    @OneToOne(mappedBy = "bloodRequest")
    private ExportLog exportLog;

    @Column(name = "request_date")
    private LocalDateTime requestDate;

    @Column(name = "deadline_date")
    private LocalDate deadlineDate;

    @Column(name = "priority")
    private String priority;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private BloodRequestStatus status;
}
