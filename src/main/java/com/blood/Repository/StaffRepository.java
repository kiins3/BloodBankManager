package com.blood.Repository;

import com.blood.Model.Staff;
import com.blood.Model.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StaffRepository extends JpaRepository<Staff,Integer> {
    Optional<Staff> findByUser(Users user);

    Optional<Staff> findByUserId(Integer id);
}
