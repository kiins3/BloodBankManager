package com.blood.repository;

import com.blood.model.EventAssignment;
import com.blood.model.enumformat.EventStatus;
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
public interface EventAssignmentRepository extends JpaRepository<EventAssignment, Integer> {

    List<EventAssignment> findByEvents_EventIdAndStatus(Integer eventId, UserStatus status);

    @Query("SELECT a FROM EventAssignment a " +
            "WHERE a.staff.staffId = :staffId " +
            "AND a.status = 'ACTIVE'")
    List<EventAssignment> findActiveAssignmentsByStaffId(@Param("staffId") Integer staffId);

    @Query("SELECT DISTINCT ea.staff.staffId FROM EventAssignment ea " +
            "WHERE ea.staff.staffId IN :staffIds " +
            "AND ea.status = :assignmentStatus " +
            "AND ea.events.status IN :eventStatuses " +
            "AND ea.events.eventId <> :excludeEventId " +
            "AND ea.events.startDate < :endDate " +
            "AND ea.events.endDate > :startDate")
    List<Integer> findBusyStaffIds(
            @Param("staffIds") List<Integer> staffIds,
            @Param("assignmentStatus") UserStatus assignmentStatus,
            @Param("eventStatuses") List<EventStatus> eventStatuses,
            @Param("excludeEventId") Integer excludeEventId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    List<EventAssignment> findByEvents_EventId(Integer eventId);

    Optional<EventAssignment> findByEvents_EventIdAndStaff_StaffId(Integer eventId, Integer staffId);

    Page<EventAssignment> findByStaff_StaffIdOrderByEvents_StartDateDesc(Integer staffId, Pageable pageable);
}