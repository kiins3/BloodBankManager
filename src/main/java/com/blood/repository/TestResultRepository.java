package com.blood.repository;

import com.blood.model.BloodBag;
import com.blood.model.TestResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TestResultRepository extends JpaRepository<TestResult,Integer> {

    Optional<TestResult> findByBloodBag(BloodBag bloodBag);


    @Query(value = "SELECT COUNT(*) FROM test_result tr " +
            "JOIN blood_bag bb ON tr.blood_bag_id = bb.blood_bag_id " +
            "JOIN event_registration er ON bb.registration_id = er.registration_id " +
            "WHERE er.donor_id = :donorId AND tr.final_conclusion = 'KHÔNG AN TOÀN'",
            nativeQuery = true)
    long countUnsafeTestResults(@Param("donorId") Integer donorId);
}
