package com.blood.Repository;

import com.blood.Model.Donor;
import com.blood.Model.Hospital;
import com.blood.Model.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HospitalRepository extends JpaRepository<Hospital,Integer> {
    Optional<Hospital> findByUser(Users user);

    Optional<Hospital> findByUserId(Integer id);
}
