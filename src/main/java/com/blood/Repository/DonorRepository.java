package com.blood.Repository;

import com.blood.Model.Donor;
import com.blood.Model.Users;
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

    Donor findByCccd(String cccd);

    @Query(value = """
        SELECT d.* FROM Donor d 
        WHERE d.blood_type = :bloodType 
          AND d.rh_factor = :rhFactor 
          AND (
               NOT EXISTS (SELECT 1 FROM Event_Registration er WHERE er.donor_id = d.donor_id)
               OR 
               (SELECT MAX(er2.created_at) FROM Event_Registration er2 WHERE er2.donor_id = d.donor_id) <= :thresholdDate
          )
        """, nativeQuery = true)    List<Donor> findEligibleDonorsToCall(@Param("bloodType") String bloodType,
                                         @Param("rhFactor") String rhFactor,
                                         @Param("thresholdDate") LocalDateTime thresholdDate);

}
