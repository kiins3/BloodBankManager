package com.blood.model;

import com.blood.model.enumformat.BloodRequestStatus;
import com.blood.model.enumformat.Priority;
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
@Table(name = "blood_request")
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

    @Enumerated(EnumType.STRING)
    @Column(name = "priority")
    private Priority priority;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private BloodRequestStatus status;

    @Column(name = "is_edited", nullable = false)
    private Boolean isEdited = false;
}
