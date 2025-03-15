package ca.mcgill.ecse321.gameorganizer.controllers;

import java.sql.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ca.mcgill.ecse321.gameorganizer.requests.CreateEventRequest;
import ca.mcgill.ecse321.gameorganizer.services.EventService;
import ca.mcgill.ecse321.gameorganizer.responses.EventResponse;
import ca.mcgill.ecse321.gameorganizer.models.Event;

@RestController
@RequestMapping("/events")
public class EventController {
    
    private final EventService eventService;
    
    @Autowired
    public EventController(EventService eventService) {
        this.eventService = eventService;
    }
    
    /**
     * @param eventId
     * @return
     */
    @GetMapping("/{eventId}")
    public ResponseEntity<EventResponse> getEvent(@PathVariable UUID eventId) {
        Event event = eventService.getEventById(eventId);
        return ResponseEntity.ok(new EventResponse(event));
    }

    /**
     * Get all events
     * @return List of all events
     */
    @GetMapping
    public ResponseEntity<List<EventResponse>> getAllEvents() {
        List<Event> events = eventService.getAllEvents();
        List<EventResponse> eventResponses = events.stream()
            .map(EventResponse::new)
            .collect(Collectors.toList());
        return ResponseEntity.ok(eventResponses);
    }
    
    @PostMapping
    public ResponseEntity<EventResponse> createEvent(@RequestBody CreateEventRequest request) {
        Event event = eventService.createEvent(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new EventResponse(event));
    }

    @PutMapping("/{eventId}")
public ResponseEntity<EventResponse> updateEvent(
        @PathVariable UUID eventId,
        @RequestParam(required = false) String title,
        @RequestParam(required = false) Date dateTime,
        @RequestParam(required = false) String location,
        @RequestParam(required = false) String description,
        @RequestParam(required = false, defaultValue = "0") int maxParticipants) {
    
    try {
        Event updatedEvent = eventService.updateEvent(
                eventId, title, dateTime, location, description, maxParticipants);
        return ResponseEntity.ok(new EventResponse(updatedEvent));
    } catch (IllegalArgumentException e) {
        return ResponseEntity.notFound().build();
    }
}
    
    @DeleteMapping("/{eventId}")
    public ResponseEntity<Void> deleteEvent(@PathVariable UUID eventId) {
        eventService.deleteEvent(eventId);
        return ResponseEntity.noContent().build();
    }
}