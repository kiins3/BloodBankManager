package com.blood.repository;

import com.blood.model.Hospital;
import com.blood.model.enumformat.UserStatus;
import com.blood.model.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HospitalRepository extends JpaRepository<Hospital,Integer> {
    Optional<Hospital> findByUser(Users user);

    Optional<Hospital> findByUserId(Integer id);

    long count();

    long countByUser_Status(UserStatus status);

    @Query("SELECT h FROM Hospital h LEFT JOIN h.user u WHERE " +
            "(:keyword IS NULL OR LOWER(h.hospitalName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "                   OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
            "(:status IS NULL OR u.status = :status)")
    Page<Hospital> findHospitalsWithFilters(
            @Param("keyword") String keyword,
            @Param("status") UserStatus status,
            Pageable pageable
    );
}
