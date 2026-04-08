package com.blood.Repository;

import com.blood.Model.ExportDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExportDetailRepository extends JpaRepository<ExportDetail,Integer> {
    boolean existsByBloodBag_BloodBagIdAndExportLog_BloodRequest_RequestId(Integer bloodBagId, Integer requestId);
}
