package ca.mcgill.ecse321.gameorganizer.services;

import java.util.Date; // Changed from java.sql.Date
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ca.mcgill.ecse321.gameorganizer.dto.request.CreateEventRequest;
import ca.mcgill.ecse321.gameorganizer.exceptions.ForbiddenException;
import ca.mcgill.ecse321.gameorganizer.exceptions.UnauthedException;
import ca.mcgill.ecse321.gameorganizer.models.Account;
import ca.mcgill.ecse321.gameorganizer.models.Event;
import ca.mcgill.ecse321.gameorganizer.models.Game;
import ca.mcgill.ecse321.gameorganizer.models.GameInstance;
import ca.mcgill.ecse321.gameorganizer.repositories.AccountRepository;
import ca.mcgill.ecse321.gameorganizer.repositories.EventRepository;
import ca.mcgill.ecse321.gameorganizer.repositories.GameInstanceRepository;
import ca.mcgill.ecse321.gameorganizer.repositories.GameRepository;

@Service
public class EventService {

    private static final Logger logger = LoggerFactory.getLogger(EventService.class);

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private GameInstanceRepository gameInstanceRepository;

    @PreAuthorize("isAuthenticated()")
    @Transactional
    public Event createEvent(CreateEventRequest newEvent) { 
        logger.debug("DEBUG SERVICE: Starting createEvent method");
        
        if (newEvent.getTitle() == null || newEvent.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Event title cannot be empty");
        }
        if (newEvent.getDateTime() == null) { // Check java.util.Date
            throw new IllegalArgumentException("Event date/time cannot be null");
        }
        if (newEvent.getLocation() == null || newEvent.getLocation().trim().isEmpty()) {
             throw new IllegalArgumentException("Event location cannot be empty");
        }
        if (newEvent.getDescription() == null || newEvent.getDescription().trim().isEmpty()) {
            throw new IllegalArgumentException("Event description cannot be empty");
        }
        if (newEvent.getMaxParticipants() <= 0) {
            throw new IllegalArgumentException("Maximum participants must be greater than 0");
        }
        if (newEvent.getFeaturedGame() == null || newEvent.getFeaturedGame().getId() <= 0) {
            throw new IllegalArgumentException("Featured game must be specified");
        }

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        logger.debug("DEBUG SERVICE: Authenticated user: {}", username);
        Account host = accountRepository.findByEmail(username)
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user account not found."));
        logger.debug("DEBUG SERVICE: Found host account: {}", host.getEmail());

        int gameId = newEvent.getFeaturedGame().getId();
        Game featuredGameEntity = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Featured game with ID " + gameId + " not found."));
        logger.debug("DEBUG SERVICE: Found featured game: {}", featuredGameEntity.getName());

        Event e = new Event(
                newEvent.getTitle(),
                newEvent.getDateTime(), // Pass java.util.Date directly
                newEvent.getLocation(),
                newEvent.getDescription(),
                newEvent.getMaxParticipants(),
                featuredGameEntity, 
                host 
        );
        
        // Handle gameInstanceId if provided (for borrowed games)
        if (newEvent.getGameInstanceId() != null) {
            logger.debug("DEBUG SERVICE: Handling game instance ID: {}", newEvent.getGameInstanceId());
            GameInstance instance = gameInstanceRepository.findById(newEvent.getGameInstanceId())
                .orElseThrow(() -> new IllegalArgumentException("Game instance with ID " + newEvent.getGameInstanceId() + " not found."));
            logger.debug("DEBUG SERVICE: Found game instance: {}", instance.getId());
            // Optional: Add validation to ensure the instance belongs to the featuredGameEntity
            if (instance.getGame().getId() != featuredGameEntity.getId()) {
                 logger.error("ERROR SERVICE: Game instance {} does not match featured game {}", instance.getId(), featuredGameEntity.getId());
                 throw new IllegalArgumentException("Provided game instance does not match the featured game.");
            }
            e.setGameInstance(instance);
            logger.debug("DEBUG SERVICE: Set game instance for event");
        }

        logger.debug("DEBUG SERVICE: Created event object, saving to repository");
        Event savedEvent = eventRepository.save(e);
        logger.debug("DEBUG SERVICE: Saved event with ID: {}", savedEvent.getId());
        return savedEvent;
    }

    @Transactional
    @PreAuthorize("@eventService.isHost(#id, authentication.principal.username)")
    public Event updateEvent(UUID id, String title, Date dateTime, // Changed from java.sql.Date
                            String location, String description, int maxParticipants) { 
        try {
            logger.debug("DEBUG SERVICE: Starting updateEvent method for ID: {}", id);
            Event event = eventRepository.findEventById(id).orElseThrow(
                    () -> new IllegalArgumentException("Event with id " + id + " does not exist")
            );
            logger.debug("DEBUG SERVICE: Found event to update: {}", event.getTitle());

            // Authorization handled by PreAuthorize

            if (title != null && !title.trim().isEmpty()) {
                logger.debug("DEBUG SERVICE: Updating title to: {}", title);
                event.setTitle(title);
            }
            if (dateTime != null) {
                logger.debug("DEBUG SERVICE: Updating dateTime to: {}", dateTime);
                event.setDateTime(dateTime); // Assign java.util.Date directly
            }
            if (location != null && !location.trim().isEmpty()) {
                 logger.debug("DEBUG SERVICE: Updating location to: {}", location);
                event.setLocation(location);
            }
            if (description != null && !description.trim().isEmpty()) {
                 logger.debug("DEBUG SERVICE: Updating description to: {}", description);
                event.setDescription(description);
            }
            if (maxParticipants > 0) { // Only update if a valid number is provided
                 logger.debug("DEBUG SERVICE: Updating maxParticipants to: {}", maxParticipants);
                event.setMaxParticipants(maxParticipants);
            }
            
            logger.debug("DEBUG SERVICE: Saving updated event with ID: {}", event.getId());
            return eventRepository.save(event);
        } catch (IllegalArgumentException e) {
             logger.error("ERROR SERVICE: Invalid argument updating event {}: {}", id, e.getMessage());
             throw e;
        } catch (org.springframework.security.access.AccessDeniedException e) {
            logger.error("ERROR SERVICE: Access denied updating event {}: {}", id, e.getMessage());
            throw new ForbiddenException("Access denied: You are not the host of this event.");
       }
    }
    
    // Method used by @PreAuthorize to check if the current user is the host
    @Transactional(readOnly = true)
    public boolean isHost(UUID eventId, String username) {
        logger.debug("DEBUG SERVICE: Checking if user {} is host of event {}", username, eventId);
        Event event = eventRepository.findEventById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event with id " + eventId + " not found for authorization check."));
        boolean isHost = event.getHost() != null && event.getHost().getEmail().equals(username);
        logger.debug("DEBUG SERVICE: Is host check result: {}", isHost);
        return isHost;
    }
    
    @Transactional(readOnly = true)
    public Event getEventById(UUID id) {
        logger.debug("DEBUG SERVICE: Getting event by ID: {}", id);
        return eventRepository.findEventById(id).orElseThrow(
                () -> new IllegalArgumentException("Event with id " + id + " does not exist")
        );
    }

    @Transactional(readOnly = true)
    public List<Event> getAllEvents() {
        logger.debug("DEBUG SERVICE: Getting all events");
        return eventRepository.findAll();
    }
    
    @Transactional(readOnly = true)
    public List<Event> getEventsByHostEmail(String email) {
        logger.debug("DEBUG SERVICE: Getting events by host email: {}", email);
        Account host = accountRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("Account with email " + email + " not found."));
        return eventRepository.findEventByHostEmail(host.getEmail()); // Changed method call
    }

    // --- Update findEventsByDate to use java.util.Date ---
    @Transactional(readOnly = true)
    public List<Event> findEventsByDate(Date date) { // Changed from java.sql.Date
        logger.debug("DEBUG SERVICE: Finding events by date: {}", date);
        // The repository method might need adjustment if it strictly expects java.sql.Date
        // However, Spring Data JPA often handles java.util.Date correctly for TIMESTAMP columns.
        // If issues arise, you might need a custom query or conversion.
        // For now, assume the repository handles java.util.Date for TIMESTAMP comparison.
        List<Event> events = eventRepository.findEventByDateTime(date); // Assuming repo method works or is adjusted
        logger.debug("DEBUG SERVICE: Found {} events for date {}", events.size(), date);

        // No conversion needed here anymore if Event model uses java.util.Date
        // for (Event event : events) { ... } 

        return events;
    }

    // --- Update other find methods similarly if they involve date comparisons ---
    // Example: findEventsByDateRange (if you add it) would also use java.util.Date

    @Transactional
    @PreAuthorize("@eventService.isHost(#id, authentication.principal.username)")
    public void deleteEvent(UUID id) {
        try {
            logger.debug("DEBUG SERVICE: Starting deleteEvent method for ID: {}", id);
            Event event = eventRepository.findEventById(id).orElseThrow(
                    () -> new IllegalArgumentException("Event with id " + id + " does not exist")
            );
            logger.debug("DEBUG SERVICE: Found event to delete: {}", event.getTitle());
            // Authorization handled by PreAuthorize
            eventRepository.delete(event);
            logger.debug("DEBUG SERVICE: Deleted event with ID: {}", id);
        } catch (IllegalArgumentException e) {
             logger.error("ERROR SERVICE: Invalid argument deleting event {}: {}", id, e.getMessage());
             throw e;
        } catch (org.springframework.security.access.AccessDeniedException e) {
            logger.error("ERROR SERVICE: Access denied deleting event {}: {}", id, e.getMessage());
            throw new ForbiddenException("Access denied: You are not the host of this event.");
       }
    }
}
