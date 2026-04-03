package com.blood.Controller;

import com.blood.DTO.Event.CreateEventRequest;
import com.blood.DTO.Event.EventResponse;
import com.blood.DTO.Event.UpdateEventRequest;
import com.blood.Repository.EventRepository;
import com.blood.Service.EventService;
import com.sun.jdi.request.EventRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/event")
public class EventController {
    @Autowired
    private EventService eventService;

    @Autowired
    private EventRepository eventRepository;

    @GetMapping("/get-list-event")
    public List<EventResponse> getEventList(){
        return eventService.getEventList();
    }

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
