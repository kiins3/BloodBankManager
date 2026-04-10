package com.blood.Model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table (name = "Storage_Equipment")
public class StorageEquipment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "equipment_id")
    private Integer equipmentId;

    @OneToMany (mappedBy = "storageEquipment")
    List<BloodBag> bloodBag;

    @Column(name = "name")
    private String name;

    @Column(name = "standard")
    private String standard;

    @Enumerated(EnumType.STRING)
    @Column(name = "product_type")
    private ProductType productType;

    @Column(name = "max_capacity")
    private Integer maxCapacity;

    @Column(name = "status")
    private String status;
}
