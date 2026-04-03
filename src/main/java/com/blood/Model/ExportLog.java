package com.blood.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "Export_Log")
public class ExportLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Integer logId;

    @ManyToOne
    @JoinColumn(name = "manager_id")
    @JsonIgnore
    private Staff manager;

    @OneToMany(mappedBy = "exportLog")
    private List<ExportDetail> exportDetails;

    @OneToOne
    @JoinColumn(name = "request_id")
    private BloodRequest bloodRequest;

    @Column(name = "export_date")
    private LocalDateTime exportDate;
}
