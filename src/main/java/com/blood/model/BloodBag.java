package com.blood.model;

import com.blood.model.enumformat.BloodBagStatus;
import com.blood.model.enumformat.ProductType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "blood_bag")
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

    @Enumerated(EnumType.STRING)
    @Column(name = "product_type")
    private ProductType productType;

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

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private BloodBagStatus status;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @Column(name = "return_reason_note", length = 255)
    private String returnReasonNote;

    @OneToMany(mappedBy = "bloodBag", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<ReturnLog> returnLogs = new ArrayList<>();

    public String getSafeStorageEquipmentName() {
        if (this.storageEquipment != null) {
            return this.storageEquipment.getName();
        }

        if (this.storageEquipment == null && this.status == BloodBagStatus.DA_TACH_CHIET){
            return "Đã tách chiết";
        }

        if (this.storageEquipment == null && this.status == BloodBagStatus.DA_XUAT) {
            return "Đã xuất";
        } else
        return "Chưa đưa vào kho";
    }
}


