package com.blood.repository;

import com.blood.model.BloodBag;
import com.blood.model.ReturnLog;
import com.blood.model.enumformat.ReturnStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReturnLogRepository extends JpaRepository<ReturnLog,Integer> {
    @Query("SELECT r FROM ReturnLog r WHERE " +
            "(:hospitalName IS NULL OR r.hospital.hospitalName LIKE %:hospitalName%) AND " +
            "(:action IS NULL OR r.actionTaken = :action)")
    Page<ReturnLog> searchReturns(@Param("hospitalName") String hospitalName,
                                  @Param("action") ReturnStatus action,
                                  Pageable pageable);

    List<ReturnLog> findByReturnOrderId(Integer returnOrderId);

    Optional<ReturnLog> findTopByBloodBagAndActionTaken(BloodBag bloodBag, ReturnStatus actionTaken);
}
