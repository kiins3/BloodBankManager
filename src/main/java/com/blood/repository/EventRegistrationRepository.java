package com.blood.repository;

import com.blood.model.Donor;
import com.blood.model.Events;
import com.blood.model.enumformat.EventRegisStatus;
import com.blood.model.EventRegistration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventRegistrationRepository  extends CrudRepository<EventRegistration, Integer> {
    boolean existsByEvents_EventIdAndDonor_DonorIdAndStatusNot(
            Integer eventId,
            Integer donorId,
            EventRegisStatus status
    );

    Optional<EventRegistration> findByEvents_EventIdAndDonor_DonorIdAndStatusNot(
            Integer eventId,
            Integer donorId,
            EventRegisStatus status
    );

    @Query("SELECT COUNT(er) FROM EventRegistration er WHERE er.status = :status AND er.createdAt >= :startOfMonth")
    long countByStatusAndCreatedAtAfter(@org.springframework.data.repository.query.Param("status") EventRegisStatus status, @Param("startOfMonth") LocalDateTime startOfMonth);

    @Query("SELECT COUNT(er) FROM EventRegistration er WHERE er.donor.donorId = :donorId AND er.status = :status")
    long countByDonorDonorIdAndStatus(@Param("donorId") Integer donorId, @Param("status") EventRegisStatus status);

    @Query("SELECT CASE WHEN COUNT(er) > 0 THEN true ELSE false END FROM EventRegistration er " +
            "WHERE er.donor.donorId = :donorId " +
            "AND er.status IN :activeStatuses " +
            "AND er.events.startDate >= :checkStart " +
            "AND er.events.startDate <= :checkEnd")
    boolean hasRegistrationWithin84Days(@Param("donorId") Integer donorId,
                                        @Param("activeStatuses") List<EventRegisStatus> activeStatuses,
                                        @Param("checkStart") LocalDateTime checkStart,
                                        @Param("checkEnd") LocalDateTime checkEnd);

    int countByEvents_EventIdAndStatus(Integer eventId, EventRegisStatus status);

    int countByEvents_EventIdAndStatusNot(Integer eventId, EventRegisStatus status);

    int countByEvents_EventIdAndStatusIn(Integer eventId, List<EventRegisStatus> statuses);

    Optional<EventRegistration> findByEvents_EventIdAndDonor_DonorId(Integer eventid, Integer donorid);

    Optional<EventRegistration> findByTicketCode(String ticketcode);

    List<EventRegistration> findByEvents_EventIdAndStatus(Integer eventId, EventRegisStatus status);

    List<EventRegistration> findByDonor_DonorIdOrderByCreatedAtDesc(Integer donorId);

    List<EventRegistration> findByEvents_EventId(Integer eventId);

    @Query("SELECT r FROM EventRegistration r JOIN FETCH r.donor d WHERE r.events.eventId = :eventId AND r.status != :excludeStatus")
    List<EventRegistration> findActiveRegistrationsByEventId(@Param("eventId") Integer eventId, @Param("excludeStatus") EventRegisStatus excludeStatus);

    boolean existsByDonor_DonorIdAndStatusAndEvents_StartDateBetween(
            Integer donorId,
            EventRegisStatus status,
            LocalDateTime startOfDay,
            LocalDateTime endOfDay
    );

    @Query("SELECT er FROM EventRegistration er " +
            "JOIN er.events e " +
            "WHERE er.donor.donorId = :donorId " +
            "AND er.status != 'DA_HUY' " +
            "AND e.endDate > :oneMonthAgo " +
            "ORDER BY CASE WHEN e.startDate > CURRENT_TIMESTAMP THEN 0 ELSE 1 END, e.startDate DESC")
    List<EventRegistration> findValidTicketsByDonorId(@Param("donorId") Integer donorId, @Param("oneMonthAgo") LocalDateTime oneMonthAgo);

    @Query("SELECT er FROM EventRegistration er " +
            "JOIN er.events e " +
            "WHERE er.donor.donorId = :donorId " +
            "AND er.status != 'DA_HUY' " +
            "AND e.endDate > :oneMonthAgo " +
            "AND (:status IS NULL OR er.status = :status) " +
            "ORDER BY CASE WHEN e.startDate > CURRENT_TIMESTAMP THEN 0 ELSE 1 END, e.startDate DESC")
    List<EventRegistration> findValidTicketsByDonorIdWithStatus(
            @Param("donorId") Integer donorId,
            @Param("oneMonthAgo") LocalDateTime oneMonthAgo,
            @Param("status") EventRegisStatus status
    );

    Page<EventRegistration> findByDonor_DonorIdOrderByEvents_StartDateDesc(Integer donorId, Pageable pageable);


    boolean existsByDonorDonorIdAndStatusNot(Integer donorId, EventRegisStatus status);

    @Query("SELECT COUNT(er) FROM EventRegistration er " +
            "WHERE er.donor.donorId = :donorId " +
            "AND er.status = :status " +
            "AND er.createdAt >= :fromDate")
    long countCancellationsInTimeRange(
            @Param("donorId") Integer donorId,
            @Param("status") EventRegisStatus status,
            @Param("fromDate") LocalDateTime fromDate
    );
}
