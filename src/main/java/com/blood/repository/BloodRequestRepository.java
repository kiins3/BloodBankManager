package com.blood.repository;

import com.blood.model.BloodRequest;
import com.blood.model.enumformat.BloodRequestStatus;
import jakarta.persistence.QueryHint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BloodRequestRepository extends JpaRepository<BloodRequest,Integer> {

    @Query("SELECT COUNT(br) FROM BloodRequest br WHERE br.status = :status")
    long countByStatus(@org.springframework.data.repository.query.Param("status") BloodRequestStatus status);
    @Query("SELECT DISTINCT br FROM BloodRequest br " +
            "LEFT JOIN FETCH br.hospital h " +
            "WHERE (:priority IS NULL OR br.priority = :priority) " +
            "AND (:status IS NULL OR br.status = :status) " +
            "AND (:hospitalName IS NULL OR h.hospitalName = :hospitalName) " +
            "ORDER BY " +
            "CASE br.status " +
            "  WHEN 'CHO_DUYET'          THEN 0 " +
            "  WHEN 'DA_DUYET_TOAN_BO'   THEN 1 " +
            "  WHEN 'DA_DUYET_MOT_PHAN'  THEN 2 " +
            "  WHEN 'DANG_VAN_CHUYEN'    THEN 3 " +
            "  WHEN 'HOAN_TRA_MOT_PHAN'  THEN 4 " +
            "  WHEN 'HOAN_TRA_TOAN_BO'   THEN 5 " +
            "  WHEN 'DA_NHAN_MOT_PHAN'   THEN 6 " +
            "  WHEN 'DA_NHAN'            THEN 7 " +
            "  WHEN 'DA_TU_CHOI'         THEN 8 " +
            "  ELSE 9 END ASC, " +
            "CASE br.priority WHEN 'KHAN_CAP' THEN 0 ELSE 1 END ASC, " +
            "br.deadlineDate ASC")
    @QueryHints(@QueryHint(name = "hibernate.query.passDistinctThrough", value = "false"))
    List<BloodRequest> findRequestsWithFilters(
            @Param("hospitalName") String hospitalName,
            @Param("status") BloodRequestStatus status,
            @Param("priority") String priority
    );

    @Query("SELECT DISTINCT b FROM BloodRequest b " +
            "LEFT JOIN FETCH b.hospital h " +
            "WHERE h.hospitalId = :hospitalId " +
            "ORDER BY " +
            "CASE b.status " +
            "  WHEN 'CHO_DUYET'          THEN 0 " +
            "  WHEN 'DA_DUYET_TOAN_BO'   THEN 1 " +
            "  WHEN 'DA_DUYET_MOT_PHAN'  THEN 2 " +
            "  WHEN 'DANG_VAN_CHUYEN'    THEN 3 " +
            "  WHEN 'HOAN_TRA_MOT_PHAN'  THEN 4 " +
            "  WHEN 'HOAN_TRA_TOAN_BO'   THEN 5 " +
            "  WHEN 'DA_NHAN_MOT_PHAN'   THEN 6 " +
            "  WHEN 'DA_NHAN'            THEN 7 " +
            "  WHEN 'DA_TU_CHOI'         THEN 8 " +
            "  ELSE 9 END ASC, " +
            "CASE b.priority WHEN 'KHAN_CAP' THEN 0 ELSE 1 END ASC, " +
            "b.deadlineDate ASC")
    @QueryHints(@QueryHint(name = "hibernate.query.passDistinctThrough", value = "false"))
    List<BloodRequest> findByHospital_HospitalIdCustomOrder(@Param("hospitalId") Integer hospitalId);

    Page<BloodRequest> findByHospital_HospitalIdOrderByRequestDateDesc(Integer hospitalId, Pageable pageable);

    long countByHospital_HospitalIdAndStatus(Integer hospitalId, BloodRequestStatus status);

    List<BloodRequest> findTop3ByHospital_HospitalIdOrderByRequestDateDesc(Integer hospitalId);
}
