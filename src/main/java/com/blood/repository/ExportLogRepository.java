package com.blood.repository;

import com.blood.model.ExportLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ExportLogRepository extends JpaRepository<ExportLog,Integer> {
    Optional<ExportLog> findByBloodRequest_RequestId(Integer requestId);
}
