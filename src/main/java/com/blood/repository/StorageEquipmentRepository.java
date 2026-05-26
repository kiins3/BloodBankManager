package com.blood.repository;

import com.blood.model.enumformat.EquipmentStatus;
import com.blood.model.enumformat.ProductType;
import com.blood.model.StorageEquipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StorageEquipmentRepository extends JpaRepository<StorageEquipment, Integer> {
    @Query("SELECT s FROM StorageEquipment s WHERE " +
            "(:equipmentId IS NULL OR s.equipmentId = :equipmentId) AND " +
            "(:productType IS NULL OR s.productType = :productType)")
    List<StorageEquipment> findWithFilter(@Param("equipmentId") Integer equipmentId,
                                          @Param("productType") ProductType productType);

    long count();

    long countByStatus(EquipmentStatus status);

    List<StorageEquipment> findByStatus(EquipmentStatus status);
}
