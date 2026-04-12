package com.blood.Repository;

import com.blood.Model.ProductType;
import com.blood.Model.StorageEquipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StorageEquipmentRepository extends JpaRepository<StorageEquipment, Integer> {
    @Query ("select s from StorageEquipment s Where" +
            "(:productType IS NULL OR s.productType = :productType)")
    List<StorageEquipment> findWithFilter(Integer equipmentId, ProductType productType);
}
