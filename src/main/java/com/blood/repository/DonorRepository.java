package com.blood.repository;

import com.blood.model.*;
import com.blood.model.enumformat.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DonorRepository extends JpaRepository<Donor,Integer> {
    Optional<Donor> findByUser(Users user);

    long count();

    long countByStatus(UserStatus status);

    Donor findByCccd(String cccd);

    Donor findByPhone(String phone);

    @Query(value = """
        SELECT d.* FROM donor d 
        WHERE d.blood_type = :bloodType 
          AND d.rh_factor = :rhFactor 
          AND (
               NOT EXISTS (SELECT 1 FROM event_registration er WHERE er.donor_id = d.donor_id)
               OR 
               (SELECT MAX(er2.created_at) FROM event_registration er2 WHERE er2.donor_id = d.donor_id) <= :thresholdDate
          )
        """, nativeQuery = true)
    List<Donor> findEligibleDonorsToCall(@Param("bloodType") String bloodType,
                                         @Param("rhFactor") String rhFactor,
                                         @Param("thresholdDate") LocalDateTime thresholdDate);

    @Query("SELECT d FROM Donor d LEFT JOIN d.user u WHERE " +
            "(:keyword IS NULL OR LOWER(d.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "                  OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
            "(:bloodType IS NULL OR d.bloodType = :bloodType) AND " +
            "(:rhFactor IS NULL OR d.rhFactor = :rhFactor) AND " +
            "(:status IS NULL OR d.status = :status)")
    Page<Donor> findDonorsWithFilters(
            @Param("keyword") String keyword,
            @Param("bloodType") String bloodType,
            @Param("rhFactor") String rhFactor,
            @Param("status") UserStatus status,
            Pageable pageable
    );

    @Query("SELECT d.bloodType, d.rhFactor, COUNT(d) FROM Donor d GROUP BY d.bloodType, d.rhFactor")
    List<Object[]> countDonorsByBloodType();

}
