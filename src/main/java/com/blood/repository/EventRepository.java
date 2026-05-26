package com.blood.repository;

import com.blood.model.enumformat.EventStatus;
import com.blood.model.Events;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Events, Integer> {

    @Query("SELECT e FROM Events e ORDER BY " +
            "CASE e.status " +
            "  WHEN com.blood.model.enumformat.EventStatus.DANG_MO THEN 1 " +
            "  WHEN com.blood.model.enumformat.EventStatus.SAP_TOI THEN 2 " +
            "  WHEN com.blood.model.enumformat.EventStatus.DA_DONG THEN 3 " +
            "  WHEN com.blood.model.enumformat.EventStatus.DA_HUY THEN 4 " +
            "  ELSE 5 END ASC, e.startDate ASC")
    List<Events> findAllEventsSortedByStatus();

    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END FROM Events e " +
            "WHERE e.location = :location " +
            "AND e.startDate < :endDate " +
            "AND e.endDate > :startDate " +
            "AND e.status != 'DA_HUY'")
    boolean existsOverlappingEvent(@Param("location") String location,
                                   @Param("startDate") LocalDateTime startDate,
                                   @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(e) FROM Events e WHERE e.status = :status")
    long countByStatus(@Param("status") EventStatus status);

    @Query("SELECT e FROM Events e ORDER BY e.startDate DESC")
    List<Events> findTop5ByOrderByStartDateDesc();

    List<Events> findByEndDateAfter(LocalDateTime now);

    List<Events> findByStatus(EventStatus eventStatus);

    List<String> findEmailsByEventId(Integer eventId);
}
