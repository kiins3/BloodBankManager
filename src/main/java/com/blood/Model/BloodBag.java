package com.blood.Model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.aspectj.weaver.ast.Test;

import java.time.LocalDateTime;

@Entity
@Table(name = "Blood_Bag")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BloodBag {
    @Id
    @Column(name = "blood_bag_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer bloodBagId;

    @Column(name = "parent_bag_id")
    private Integer parentBagId;

    @ManyToOne (fetch = FetchType.LAZY)
    @JoinColumn (name = "registration_id")
    private EventRegistration registration;

    @OneToOne(mappedBy = "bloodBag", cascade = CascadeType.ALL)
    private TestResult testResult;

    @ManyToOne (fetch = FetchType.LAZY)
    @JoinColumn(name = "stored_by")
    private Staff staff;

    @OneToOne(mappedBy = "bloodBag")
    private ExportDetail exportDetail;

    @Column(name = "collected_at")
    private LocalDateTime collectedAt;

    @Column(name = "expiration_date")
    private LocalDateTime expiredAt;

    @Column(name = "blood_type")
    private String bloodType;

    @Column(name = "rh_factor")
    private String rhFactor;

    @Column(name = "product_type")
    private String productType;

    @Column(name = "volume")
    private Integer volume;

    @Column(name = "product_volume")
    private Integer productVolume;

    @Column(name = "bag_code")
    private String bagCode;

    @ManyToOne (fetch = FetchType.LAZY)
    @JoinColumn(name = "equipment_id")
    private StorageEquipment storageEquipment;

    @Column(name = "stored_at")
    private LocalDateTime storedAt;

    @Column(name = "status")
    private String status;

    public String getSafeStorageEquipmentName() {
        if (this.storageEquipment != null) {
            return this.storageEquipment.getName();
        }
        return "Chưa đưa vào kho";
    }
}


