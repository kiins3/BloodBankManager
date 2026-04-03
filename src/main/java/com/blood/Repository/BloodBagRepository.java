package com.blood.Repository;

import com.blood.Model.BloodBag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BloodBagRepository extends JpaRepository<BloodBag,Integer> {
    @Query("SELECT b FROM BloodBag b WHERE " +
            "(:bloodBagId IS NULL OR b.bloodBagId = :bloodBagId) AND " +
            "(:bloodType IS NULL OR UPPER(b.bloodType) = UPPER(:bloodType)) AND " +
            "(:rhFactor IS NULL OR b.rhFactor = :rhFactor) AND " +
            "(:productType IS NULL OR b.productType = :productType) AND " +
            "(:status IS NULL OR b.status = :status)")
    List<BloodBag> findWithFilters(@Param("bloodBagId") Integer bloodBagId,
                                   @Param("bloodType") String bloodType,
                                   @Param("rhFactor") String rhFactor,
                                   @Param("productType") String productType,
                                   @Param("status") String status);

    int countByStorageEquipment_EquipmentId(Integer equipmentId);
}
