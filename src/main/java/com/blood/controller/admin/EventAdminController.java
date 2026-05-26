package com.blood.controller.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.blood.dto.Event.CreateEventRequest;
import com.blood.dto.Event.EventResponse;
import com.blood.dto.Event.UpdateEventRequest;
import com.blood.service.EventService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/event")
@SecurityRequirement(name = "bearerAuth")
@Slf4j
@RequiredArgsConstructor
public class EventAdminController {

    private final EventService eventService;

    @PostMapping("/create-event")
    public ResponseEntity<?> createEvent(@RequestBody CreateEventRequest rq){
        eventService.createEvent(rq);
        return ResponseEntity.ok("Tạo sự kiện mới thành công");
    }

    @PutMapping("/update-event/{eventId}")
    public EventResponse updateEvent(@PathVariable Integer eventId, @RequestBody UpdateEventRequest rq){
        return eventService.updateEvent(eventId, rq);
    }

    @PatchMapping("/cancel-event/{eventId}")
    public void cancelEvent(@PathVariable Integer eventId) {
        eventService.cancelEvent(eventId);
    }
}
