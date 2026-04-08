package com.blood.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExportDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "detail_id")
    private Integer detailId;

    @ManyToOne (fetch = FetchType.LAZY)
    @JoinColumn(name = "log_id")
    @JsonIgnore
    private ExportLog  exportLog;

    @OneToOne
    @JoinColumn(name = "blood_bag_id")
    private BloodBag bloodBag;
}
