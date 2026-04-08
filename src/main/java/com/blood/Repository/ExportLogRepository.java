package com.blood.Repository;

import com.blood.Model.ExportLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExportLogRepository extends JpaRepository<ExportLog,Integer> {
    java.util.Optional<ExportLog> findByBloodRequest_RequestId(Integer requestId);
}
