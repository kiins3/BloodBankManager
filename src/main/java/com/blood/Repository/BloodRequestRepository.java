package com.blood.Repository;

import com.blood.Model.BloodRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BloodRequestRepository extends JpaRepository<BloodRequest,Integer> {
    @Query("SELECT br FROM BloodRequest br " +
            "LEFT JOIN FETCH br.exportLog " + // Dòng này là "phép thuật" dọn sạch lỗi N+1
            "LEFT JOIN FETCH br.hospital h " +
            "WHERE (:status IS NULL OR br.status = :status) " +
            "AND (:hospitalName IS NULL OR h.hospitalName = :hospitalName)")
    List<BloodRequest> findRequestsWithFilters(
            @Param("status") String status,
            @Param("hospitalName") String hospitalName
    );

    @Query("SELECT b FROM BloodRequest b WHERE b.hospital.hospitalId = :hospitalId " +
            "ORDER BY (CASE WHEN b.priority = com.blood.Model.Priority.KHAN_CAP THEN 0 ELSE 1 END) ASC, " +
            "b.deadlineDate ASC")
    List<BloodRequest> findByHospital_HospitalIdCustomOrder(@Param("hospitalId") Integer hospitalId);
}
