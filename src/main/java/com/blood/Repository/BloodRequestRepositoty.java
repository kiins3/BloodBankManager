package com.blood.Repository;

import com.blood.Model.BloodRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BloodRequestRepositoty extends JpaRepository<BloodRequest,Integer> {
    @Query("SELECT b FROM BloodRequest b JOIN b.hospital h WHERE " +
            ":hospitalName IS NULL OR h.hospitalName = :hospitalName AND " +
            "(:status IS NULL OR b.status = :status)")
    List<BloodRequest> findWithFilters(@Param("hospitalName") String hospitalName,
                                       @Param("status") String status);

    List<BloodRequest> findByHospital_HospitalId(Integer hospitalId);
}
