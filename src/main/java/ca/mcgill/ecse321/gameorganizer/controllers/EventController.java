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

import ca.mcgill.ecse321.gameorganizer.dto.CreateEventRequest;
import ca.mcgill.ecse321.gameorganizer.dto.EventResponse;
import ca.mcgill.ecse321.gameorganizer.middleware.RequireUser;
import ca.mcgill.ecse321.gameorganizer.models.Event;
import ca.mcgill.ecse321.gameorganizer.services.EventService;

/**
 * REST controller for managing gaming events.
 * Provides endpoints for creating, retrieving, updating, and deleting events,
 * as well as searching events by various criteria.
 * @author @Yessine-glitch
 */
@RestController
@RequestMapping("/api/v1/events")
public class EventController {
    
    private final EventService eventService;
    
    @Autowired
    public EventController(EventService eventService) {
        this.eventService = eventService;
    }
    
    /**
     * Retrieves an event by its ID.
     * 
     * @param eventId The UUID of the event to retrieve
     * @return The event with the specified ID
     */
    @GetMapping("/{eventId}")
    public ResponseEntity<EventResponse> getEvent(@PathVariable UUID eventId) {
        Event event = eventService.getEventById(eventId);
        return ResponseEntity.ok(new EventResponse(event));
    }

    /**
     * Retrieves all events.
     * 
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
    
    /**
     * Creates a new event.
     * 
     * @param request The event creation request
     * @return The created event
     */
    @PostMapping
    public ResponseEntity<EventResponse> createEvent(@RequestBody CreateEventRequest request) {
        Event event = eventService.createEvent(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new EventResponse(event));
    }

    /**
     * Updates an existing event.
     * 
     * @param eventId The UUID of the event to update
     * @param title The new title (optional)
     * @param dateTime The new date and time (optional)
     * @param location The new location (optional)
     * @param description The new description (optional)
     * @param maxParticipants The new maximum number of participants (optional)
     * @return The updated event
     */
    @RequireUser
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
    
    /**
     * Deletes an event by its ID.
     * 
     * @param eventId The UUID of the event to delete
     * @return No content response
     */
    @DeleteMapping("/{eventId}")
    public ResponseEntity<Void> deleteEvent(@PathVariable UUID eventId) {
        eventService.deleteEvent(eventId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Finds events scheduled on a specific date.
     * 
     * @param date The date to search for
     * @return List of events on the specified date
     */
    @GetMapping("/by-date")
    public ResponseEntity<List<EventResponse>> getEventsByDate(@RequestParam Date date) {
        List<Event> events = eventService.findEventsByDate(date);
        List<EventResponse> eventResponses = events.stream()
            .map(EventResponse::new)
            .collect(Collectors.toList());
        return ResponseEntity.ok(eventResponses);
    }
    
    /**
     * Finds events featuring a specific game by the game's ID.
     * 
     * @param gameId The ID of the featured game
     * @return List of events featuring the specified game
     */
    @GetMapping("/by-game-id/{gameId}")
    public ResponseEntity<List<EventResponse>> getEventsByGameId(@PathVariable int gameId) {
        List<Event> events = eventService.findEventsByGameId(gameId);
        List<EventResponse> eventResponses = events.stream()
            .map(EventResponse::new)
            .collect(Collectors.toList());
        return ResponseEntity.ok(eventResponses);
    }
    
    /**
     * Finds events featuring a specific game by the game's name.
     * 
     * @param gameName The name of the featured game
     * @return List of events featuring the specified game
     */
    @GetMapping("/by-game-name")
    public ResponseEntity<List<EventResponse>> getEventsByGameName(@RequestParam String gameName) {
        try {
            List<Event> events = eventService.findEventsByGameName(gameName);
            List<EventResponse> eventResponses = events.stream()
                .map(EventResponse::new)
                .collect(Collectors.toList());
            return ResponseEntity.ok(eventResponses);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Finds events hosted by a specific user by the host's ID.
     * 
     * @param hostId The ID of the host
     * @return List of events hosted by the specified user
     */
    @GetMapping("/by-host-id/{hostId}")
    public ResponseEntity<List<EventResponse>> getEventsByHostId(@PathVariable int hostId) {
        List<Event> events = eventService.findEventsByHostId(hostId);
        List<EventResponse> eventResponses = events.stream()
            .map(EventResponse::new)
            .collect(Collectors.toList());
        return ResponseEntity.ok(eventResponses);
    }
    
    /**
     * Finds events hosted by a specific user by the host's username.
     * 
     * @param hostUsername The username of the host
     * @return List of events hosted by the specified user
     */
    @GetMapping("/by-host-name")
    public ResponseEntity<List<EventResponse>> getEventsByHostName(@RequestParam String hostUsername) {
        try {
            List<Event> events = eventService.findEventsByHostName(hostUsername);
            List<EventResponse> eventResponses = events.stream()
                .map(EventResponse::new)
                .collect(Collectors.toList());
            return ResponseEntity.ok(eventResponses);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Finds events where the featured game has a specific minimum number of players.
     * 
     * @param minPlayers The minimum number of players for the featured game
     * @return List of events with games matching the minimum player count
     */
    @GetMapping("/by-game-min-players/{minPlayers}")
    public ResponseEntity<List<EventResponse>> getEventsByGameMinPlayers(@PathVariable int minPlayers) {
        try {
            List<Event> events = eventService.findEventsByGameMinPlayers(minPlayers);
            List<EventResponse> eventResponses = events.stream()
                .map(EventResponse::new)
                .collect(Collectors.toList());
            return ResponseEntity.ok(eventResponses);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Finds events by location, with partial matching supported.
     * 
     * @param location The location text to search for
     * @return List of events at locations matching the search text
     */
    @GetMapping("/by-location")
    public ResponseEntity<List<EventResponse>> getEventsByLocation(@RequestParam String location) {
        try {
            List<Event> events = eventService.findEventsByLocationContaining(location);
            List<EventResponse> eventResponses = events.stream()
                .map(EventResponse::new)
                .collect(Collectors.toList());
            return ResponseEntity.ok(eventResponses);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}