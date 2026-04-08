package com.blood.Model;

import jakarta.persistence.*;
import jdk.jfr.MemoryAddress;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "Request_Detail")
public class RequestDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "detail_id")
    private Integer detailId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id")
    private BloodRequest bloodRequest;

    @Column(name = "blood_type")
    private String bloodType;

    @Column(name = "product_type")
    private String productType;

    @Column(name = "volume")
    private Integer volume;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "approved_quantity")
    private Integer approvedQuantity;
}
