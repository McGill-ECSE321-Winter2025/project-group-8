package ca.mcgill.ecse321.gameorganizer.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import ca.mcgill.ecse321.gameorganizer.requests.CreateEventRequest;
import ca.mcgill.ecse321.gameorganizer.services.EventService;
import ca.mcgill.ecse321.gameorganizer.responses.EventResponse;
import ca.mcgill.ecse321.gameorganizer.models.Event;

@RestController
@RequestMapping("/events")
public class EventController {
    @Autowired
    private EventService eventService;

    @GetMapping("/events/{eventId}")
    public EventResponse getEvent(@PathVariable int eventId) {
        Event event = eventService.getEventById(eventId);
        return new EventResponse(event);
    }

    @PostMapping("/events")
    @ResponseStatus(HttpStatus.CREATED)
    public EventResponse createEvent(@RequestBody CreateEventRequest request) {
        Event event = eventService.createEvent(request);
        return new EventResponse(event);
    }

}
