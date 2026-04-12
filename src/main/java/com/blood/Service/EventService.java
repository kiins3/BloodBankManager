package com.blood.Service;

import com.blood.DTO.Event.CreateEventRequest;
import com.blood.DTO.Event.EventResponse;
import com.blood.DTO.Event.UpdateEventRequest;
import com.blood.Model.EventStatus;
import com.blood.Model.Events;
import com.blood.Repository.EventRepository;
import com.sun.jdi.request.EventRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EventService {
    @Autowired
    private EventRepository eventRepository;

    public List<EventResponse> getEventList() {
        List<Events> events = eventRepository.findAll();

        return events.stream().map(event -> convertToDTOGetList(event)).collect(Collectors.toList());
    }

    public EventResponse convertToDTOGetList(Events event){
        int count = 0;
        if (event.getRegistrations() != null) {
            count = event.getRegistrations().size();
        }

        EventStatus status = EventStatus.SAP_TOI;
        LocalDateTime date = LocalDateTime.now();
        if (event.getEndDate().isBefore(date)) {
            status = EventStatus.DA_DONG;
        } else if (event.getStartDate().isBefore(date) && event.getEndDate().isAfter(date)) {
            status = EventStatus.DANG_MO;
        } else status = EventStatus.SAP_TOI;

        return EventResponse.builder()
                .eventId(event.getEventId())
                .eventName(event.getEventName())
                .startDate(event.getStartDate())
                .endDate(event.getEndDate())
                .location(event.getLocation())
                .targetAmount(event.getTargetAmount())
                .currentAmount(count)
                .status(status).build();
    }

    public void createEvent(CreateEventRequest rq) {
        Events event = new Events();
        event.setEventName(rq.getEventName());
        event.setLocation(rq.getLocation());
        event.setStartDate(rq.getStartDate());
        event.setEndDate(rq.getEndDate());
        event.setTargetAmount(rq.getTargetAmount());
        event.setStatus(rq.getStatus());

        eventRepository.save(event);
    }

    public EventResponse updateEvent(Integer id, UpdateEventRequest rq) {
        Events event = eventRepository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy chiến dịch"));

        event.setEventName(rq.getEventName());
        event.setTargetAmount(rq.getTargetAmount());
        event.setStatus(rq.getStatus());

        Events updateEvent = eventRepository.save(event);

        EventResponse eventResponse = convertToDTO(updateEvent);
        return eventResponse;
    }

    private EventResponse convertToDTO(Events event) {
        int count = 0;
        if (event.getRegistrations() != null) {
            count = event.getRegistrations().size();
        }
        return EventResponse.builder()
                .eventId(event.getEventId())
                .eventName(event.getEventName())
                .startDate(event.getStartDate())
                .endDate(event.getEndDate())
                .location(event.getLocation())
                .targetAmount(event.getTargetAmount())
                .currentAmount(count)
                .status(event.getStatus())
                .build();
    }


}
