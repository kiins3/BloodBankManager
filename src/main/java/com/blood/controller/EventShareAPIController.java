package com.blood.controller;

import com.blood.dto.Admin.EventDetailResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.blood.dto.Event.EventResponse;
import com.blood.repository.EventRepository;
import com.blood.service.EventService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/shared/event")
@SecurityRequirement(name = "bearerAuth")
@Slf4j
@RequiredArgsConstructor
public class EventShareAPIController {

    private final EventService eventService;

    @GetMapping("/get-list-event")
    public List<EventResponse> getEventList(){
        return eventService.getEventList();
    }

    @GetMapping("/list-event-coming")
    public List<EventResponse> getComingEvents() {
        return eventService.getComingEvents();
    }

    @GetMapping("/event-detail/{eventId}")
    public ResponseEntity<?> getEventDetail(@PathVariable Integer eventId) {
        EventDetailResponse response = eventService.getEventDetail(eventId);
        return ResponseEntity.ok(response);
    }
}
