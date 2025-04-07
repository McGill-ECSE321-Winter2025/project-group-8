package ca.mcgill.ecse321.gameorganizer.controllers;

import java.sql.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired; // Import Authentication
import org.springframework.http.HttpStatus; // Import SecurityContextHolder
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication; // Keep for logging context holder
import org.springframework.security.core.context.SecurityContextHolder; // Import Principal
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
import ca.mcgill.ecse321.gameorganizer.models.Event;
import ca.mcgill.ecse321.gameorganizer.services.EventService;

/**
 * REST controller for managing gaming events.
 * Provides endpoints for creating, retrieving, updating, and deleting events,
 * as well as searching events by various criteria.
 * @author @Yessine-glitch
 */
@RestController
@RequestMapping("/events")
public class EventController {
    
    private static final Logger log = LoggerFactory.getLogger(EventController.class);
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
      * @param authentication The authenticated user details
     * @return The created event
     */
    @PostMapping
    public ResponseEntity<EventResponse> createEvent(@RequestBody CreateEventRequest request) { // Removed Principal parameter
        // Get Authentication directly from SecurityContextHolder
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        log.info("SecurityContextHolder Authentication at start of createEvent: {}", authentication);

        // Check if Authentication object is valid
        if (authentication == null || !authentication.isAuthenticated() || authentication.getName() == null) {
             log.error("Create Event: Could not retrieve valid Authentication from SecurityContextHolder. User not authenticated.");
             return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String email = authentication.getName(); // Get email from the retrieved Authentication object
        log.info("Attempting to create event for user: {}", email);

         // Check if email is null or empty (redundant if getName() worked, but safe)
         if (email.trim().isEmpty()) {
             log.error("Create Event: Email extracted from Authentication is empty.");
             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build(); // Indicate an unexpected state
         }

        Event event = eventService.createEvent(request, email);
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
      * @param authentication The authenticated user details
     * @return The updated event
     */
    @PutMapping("/{eventId}")
    public ResponseEntity<EventResponse> updateEvent( // Corrected signature start
            @PathVariable UUID eventId,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) java.sql.Date dateTime,
 // Use explicit java.sql.Date
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String description,
            @RequestParam(required = false, defaultValue = "0") int maxParticipants,
            Authentication authentication) { // Corrected signature end
        
        log.info("Attempting to update event {}. Authentication: {}", eventId, authentication);
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                 log.error("Update Event {}: User not authenticated.", eventId);
                 return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            String email = authentication.getName(); // Get email from Authentication
            Event updatedEvent = eventService.updateEvent(
                   eventId, title, dateTime, location, description, maxParticipants, email);
            return ResponseEntity.ok(new EventResponse(updatedEvent));
        } catch (IllegalArgumentException e) {
            // This specific catch block might become redundant with the @ExceptionHandler,
            // but leaving it for now doesn't hurt. The handler will take precedence.
            return ResponseEntity.notFound().build(); 
        }
    }
    
    /**
     * Deletes an event by its ID.
     * 
     * @param eventId The UUID of the event to delete
      * @param authentication The authenticated user details
     * @return No content response
     */
    @DeleteMapping("/{eventId}")
    public ResponseEntity<Void> deleteEvent(@PathVariable UUID eventId) {
        
        eventService.deleteEvent(eventId); // Service handles exceptions (NotFound, Forbidden)
        return ResponseEntity.noContent().build(); // Controller returns 204 on success
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
        // Let the exception handler manage potential IllegalArgumentException from service
        List<Event> events = eventService.findEventsByGameName(gameName);
        List<EventResponse> eventResponses = events.stream()
            .map(EventResponse::new)
            .collect(Collectors.toList());
        return ResponseEntity.ok(eventResponses);
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
    // Changed path and parameter name to reflect search by email
    @GetMapping("/by-host-email")
    public ResponseEntity<List<EventResponse>> getEventsByHostEmail(@RequestParam String hostEmail) {
        // Let the exception handler manage potential IllegalArgumentException from service
        List<Event> events = eventService.findEventsByHostEmail(hostEmail); // Call updated service method
        List<EventResponse> eventResponses = events.stream()
            .map(EventResponse::new)
            .collect(Collectors.toList());
        return ResponseEntity.ok(eventResponses);
    }
    
    /**
     * Finds events where the featured game has a specific minimum number of players.
     * 
     * @param minPlayers The minimum number of players for the featured game
     * @return List of events with games matching the minimum player count
     */
    @GetMapping("/by-game-min-players/{minPlayers}")
    public ResponseEntity<List<EventResponse>> getEventsByGameMinPlayers(@PathVariable int minPlayers) {
        // Let the exception handler manage potential IllegalArgumentException from service
        List<Event> events = eventService.findEventsByGameMinPlayers(minPlayers);
        List<EventResponse> eventResponses = events.stream()
            .map(EventResponse::new)
            .collect(Collectors.toList());
        return ResponseEntity.ok(eventResponses);
    }
    
    /**
     * Finds events by location, with partial matching supported.
     * 
     * @param location The location text to search for
     * @return List of events at locations matching the search text
     */
    @GetMapping("/by-location")
    public ResponseEntity<List<EventResponse>> getEventsByLocation(@RequestParam String location) {
        // Let the exception handler manage potential IllegalArgumentException from service
        List<Event> events = eventService.findEventsByLocationContaining(location);
        List<EventResponse> eventResponses = events.stream()
            .map(EventResponse::new)
            .collect(Collectors.toList());
        return ResponseEntity.ok(eventResponses);
    }

    /**
     * Finds events by title, with partial matching supported.
     * 
     * @param title The title text to search for
     * @return List of events with titles matching the search text
     */
    @GetMapping("/by-title")
    public ResponseEntity<List<EventResponse>> getEventsByTitle(@RequestParam String title) {
        try {
            List<Event> events = eventService.findEventByTitle(title);
            List<EventResponse> eventResponses = events.stream()
                .map(EventResponse::new)
                .collect(Collectors.toList());
            return ResponseEntity.ok(eventResponses);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
