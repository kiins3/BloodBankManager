package com.blood.Controller;

import com.blood.DTO.Event.EventResponse;
import com.blood.Repository.EventRepository;
import com.blood.Service.EventService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/shared/event")
@SecurityRequirement(name = "bearerAuth")
public class EventShareAPIController {

    @Autowired
    private EventService eventService;

    @Autowired
    private EventRepository eventRepository;

    @GetMapping("/get-list-event")
    public List<EventResponse> getEventList(){
        return eventService.getEventList();
    }
}
