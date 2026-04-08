package com.blood.Repository;

import com.blood.Model.BloodBag;
import com.blood.Model.TestResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TestResultRepository extends JpaRepository<TestResult,Integer> {

    Optional<TestResult> findByBloodBag(BloodBag bloodBag);
}
