package com.blood.repository;

import com.blood.model.BloodBag;
import com.blood.model.enumformat.BloodBagStatus;
import com.blood.model.enumformat.ProductType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BloodBagRepository extends JpaRepository<BloodBag,Integer> {

    @Query("SELECT COUNT(b) FROM BloodBag b")
    long countAllBloodBags();

    @Query("SELECT COUNT(b) FROM BloodBag b WHERE b.status = 'SAN_SANG' AND b.expiredAt BETWEEN CURRENT_TIMESTAMP AND :threshold")
    long countExpiringBloodBags(@Param("threshold") LocalDateTime threshold);

    @Query("SELECT COUNT(b) FROM BloodBag b WHERE b.status = :status")
    long countByStatus(@Param("status") BloodBagStatus status);

    @Query("SELECT COUNT(b) FROM BloodBag b WHERE b.status = 'SAN_SANG' AND b.expiredAt < CURRENT_TIMESTAMP")
    long countExpiredBloodBags();

    @Query("""
    SELECT b FROM BloodBag b
    WHERE
        (:bloodBagId IS NULL OR b.bloodBagId = :bloodBagId) AND
        (:bloodType  IS NULL OR UPPER(b.bloodType) = UPPER(:bloodType)) AND
        (:rhFactor   IS NULL OR b.rhFactor = :rhFactor) AND
        (:productType IS NULL OR b.productType = :productType) AND
        (:status IS NULL OR b.status = :status)
    ORDER BY
        CASE b.status
            WHEN 'CHO_HUY'         THEN 1
            WHEN 'CHO_KIEM_DINH'   THEN 2
            WHEN 'CHO_XET_NGHIEM'  THEN 3
            WHEN 'CHO_TACH_CHIET'  THEN 4
            WHEN 'CHO_IN_NHAN'     THEN 5
            WHEN 'CHO_BAO_QUAN'    THEN 6
            WHEN 'DA_TACH_CHIET'   THEN 7
            WHEN 'SAN_SANG'        THEN 8
            WHEN 'DA_XUAT'         THEN 9
            WHEN 'HOAN_TRA'        THEN 10
            WHEN 'DA_DUOC_SU_DUNG' THEN 11
            WHEN 'DA_HUY'          THEN 12
            ELSE 13
        END ASC,
        b.expiredAt ASC NULLS LAST,
        b.bloodBagId ASC
    """)
    List<BloodBag> findWithFilters(
            @Param("bloodBagId")  Integer bloodBagId,
            @Param("bloodType")   String bloodType,
            @Param("rhFactor")    String rhFactor,
            @Param("productType") ProductType productType,
            @Param("status")      BloodBagStatus status);

    @Query("SELECT COUNT(b) FROM BloodBag b WHERE b.storageEquipment.equipmentId = :equipmentId AND b.status = :status")
    int countActiveBagsInEquipment(@Param("equipmentId") Integer equipmentId, @Param("status") BloodBagStatus status);

    Optional<BloodBag> findByBagCode(String bagCode);

    @Query("SELECT b FROM BloodBag b " +
            "WHERE b.status = 'SAN_SANG' " +
            "AND b.expiredAt > CURRENT_TIMESTAMP " +
            "AND b.productType = :productType " +
            "AND b.bloodType = :bloodType " +
            "AND b.rhFactor = :rhFactor " +
            "AND b.volume = :volume " +
            "ORDER BY b.expiredAt ASC")
    List<BloodBag> findBagsForExport(
            @Param("productType") ProductType productType,
            @Param("bloodType") String bloodType,
            @Param("rhFactor") String rhFactor,
            @Param("volume") Integer volume,
            Pageable pageable
    );

    @Query("SELECT b.bloodType as bloodType, b.rhFactor as rhFactor, COUNT(b) as total " +
            "FROM BloodBag b WHERE b.status = 'SAN_SANG' " +
            "GROUP BY b.bloodType, b.rhFactor")
    List<BloodCountProjection> countAvailableBloodBags();

    @Query("SELECT b FROM BloodBag b WHERE b.status = 'CHO_BAO_QUAN' AND b.expiredAt <= :threshold")
    List<BloodBag> findExpiringAndExpiredBags(@Param("threshold") LocalDateTime threshold);

    @Query("SELECT ed.bloodBag FROM ExportDetail ed WHERE ed.exportLog.bloodRequest.requestId = :requestId")
    List<BloodBag> findBloodBagsByRequestId(@Param("requestId") Integer requestId);
}


