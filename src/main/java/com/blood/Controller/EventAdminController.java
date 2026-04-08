package com.blood.Controller;

import com.blood.DTO.Event.CreateEventRequest;
import com.blood.DTO.Event.EventResponse;
import com.blood.DTO.Event.UpdateEventRequest;
import com.blood.Service.EventService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/event")
@SecurityRequirement(name = "bearerAuth")
public class EventAdminController {
    @Autowired
    private EventService eventService;

    @PostMapping("/create-event")
    public ResponseEntity<?> createEvent(@RequestBody CreateEventRequest rq){
        eventService.createEvent(rq);
        return ResponseEntity.ok("Tạo sự kiện mới thành công");
    }

    @PutMapping("/update-event/{id}")
    public EventResponse updateEvent(@PathVariable Integer id, @RequestBody UpdateEventRequest rq){
        return eventService.updateEvent(id, rq);
    }
}
