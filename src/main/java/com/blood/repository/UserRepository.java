package com.blood.repository;

import com.blood.model.Users;
import com.blood.model.enumformat.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<Users, Integer> {
    Optional<Users> findByEmail(String email);

    Boolean existsByEmail(String email);


    boolean existsByRole(Role role);
}
