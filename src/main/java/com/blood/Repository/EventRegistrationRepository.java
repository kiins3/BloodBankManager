package com.blood.Repository;

import com.blood.Model.EventRegistration;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventRegistrationRepository  extends CrudRepository<EventRegistration, Integer> {
    boolean existsByEvents_EventIdAndDonor_DonorId(Integer eventId, Integer donorId);

    int countByEvents_EventIdAndStatus(Integer eventId, String status);

    Optional<EventRegistration> findByEvents_EventIdAndDonor_DonorId(Integer eventid, Integer donorid);

    Optional<EventRegistration> findByTicketCode(String ticketcode);

    List<EventRegistration> findByEvents_EventIdAndStatus(Integer eventId, String status);
}
