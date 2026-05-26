package com.blood.repository;

import com.blood.dto.Staff.StaffAvailabilityResponse;
import com.blood.model.enumformat.Position;
import com.blood.model.Staff;
import com.blood.model.enumformat.UserStatus;
import com.blood.model.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface StaffRepository extends JpaRepository<Staff,Integer> {
    Optional<Staff> findByUser(Users user);

    Optional<Staff> findByUserId(Integer id);

    long count();

    long countByPosition(Position position);
    long countByPositionIn(Collection<Position> positions);
    long countByStatus(UserStatus status);

    @Query("SELECT s FROM Staff s LEFT JOIN s.user u WHERE " +
            "(:keyword IS NULL OR LOWER(s.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "                  OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
            "(:position IS NULL OR s.position = :position) AND " +
            "(:status IS NULL OR s.status = :status)")
    Page<Staff> findStaffsWithFilters(
            @Param("keyword") String keyword,
            @Param("position") Position position,
            @Param("status") UserStatus status,
            Pageable pageable
    );

    @Query("SELECT new com.blood.dto.Staff.StaffAvailabilityResponse(" +
            "s.staffId, s.fullName, s.position, " +
            "CASE WHEN EXISTS (" +
            "SELECT 1 FROM EventAssignment ea JOIN ea.events e " +
            "WHERE ea.staff.staffId = s.staffId " +
            "AND ea.status = 'ACTIVE' " +
            "AND e.status IN ('SAP_TOI', 'DANG_MO')" +
            ") THEN false ELSE true END) " +
            "FROM Staff s " +
            "WHERE s.position != 'QUAN_LY_KHO' " +
            "ORDER BY " +
            "CASE WHEN EXISTS (" +
            "SELECT 1 FROM EventAssignment ea JOIN ea.events e " +
            "WHERE ea.staff.staffId = s.staffId " +
            "AND ea.status = 'ACTIVE' " +
            "AND e.status IN ('SAP_TOI', 'DANG_MO')" +
            ") THEN 1 ELSE 0 END, " +
            "s.fullName ASC")
    List<StaffAvailabilityResponse> findSmartStaffListForAssignment();

}
