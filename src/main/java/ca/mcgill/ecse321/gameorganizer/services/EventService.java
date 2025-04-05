package ca.mcgill.ecse321.gameorganizer.services;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.access.prepost.PreAuthorize; // Import PreAuthorize
import ca.mcgill.ecse321.gameorganizer.exceptions.ForbiddenException; // Import ForbiddenException

import ca.mcgill.ecse321.gameorganizer.dto.CreateEventRequest;
import ca.mcgill.ecse321.gameorganizer.exceptions.UnauthedException;
import ca.mcgill.ecse321.gameorganizer.models.Account; // Import UnauthedException
import ca.mcgill.ecse321.gameorganizer.models.Event; // Import Account
import ca.mcgill.ecse321.gameorganizer.models.Game; // Added missing Game import
import ca.mcgill.ecse321.gameorganizer.repositories.AccountRepository; // Added AccountRepository import
import ca.mcgill.ecse321.gameorganizer.repositories.EventRepository;
import ca.mcgill.ecse321.gameorganizer.repositories.GameRepository;



/**
 * Service class that handles business logic for event management operations.
 * Provides methods for creating, retrieving, updating, and deleting gaming events.
 * Ensures business rules and validation for event operations.
 *
 * @author @Yessine-glitch
 */
@Service
public class EventService {

    private final EventRepository eventRepository;

    @Autowired
    private GameRepository gameRepository;

    // UserContext field removed

    @Autowired
    private AccountRepository accountRepository; // Inject AccountRepository

    /**
     * Constructs an EventService with the required repository dependency.
     *
     * @param eventRepository The repository for event data access
     */
    // Updated constructor to inject AccountRepository instead of UserContext
    @Autowired
    public EventService(EventRepository eventRepository, AccountRepository accountRepository, GameRepository gameRepository) {
        this.eventRepository = eventRepository;
        this.accountRepository = accountRepository;
        this.gameRepository = gameRepository; // Ensure GameRepository is also initialized if needed elsewhere
    }

    /**
     * Creates a new event in the system after validating required fields.
     *
     * @param newEvent The DTO containing event details (excluding host).
     * @param hostEmail The email of the account hosting the event.
     * @return The created Event object.
     * @throws IllegalArgumentException if required fields are missing or invalid
     */
    @PreAuthorize("isAuthenticated()")
    @Transactional
    public Event createEvent(CreateEventRequest newEvent) { // Removed hostEmail parameter
        // Removed debug line referencing hostEmail
        System.out.println("DEBUG SERVICE: newEvent.getTitle()=" + newEvent.getTitle());
        System.out.println("DEBUG SERVICE: newEvent.getDateTime()=" + newEvent.getDateTime());
        System.out.println("DEBUG SERVICE: newEvent.getMaxParticipants()=" + newEvent.getMaxParticipants());
        System.out.println("DEBUG SERVICE: newEvent.getFeaturedGame()=" + 
                (newEvent.getFeaturedGame() != null ? 
                "id=" + newEvent.getFeaturedGame().getId() + ", name=" + newEvent.getFeaturedGame().getName() : "null"));
        
        if (newEvent.getHost() != null) {
            System.out.println("DEBUG SERVICE: newEvent.getHost()=" + 
                "id=" + newEvent.getHost().getId() + ", email=" + newEvent.getHost().getEmail());
        } else {
            System.out.println("DEBUG SERVICE: newEvent.getHost()=null");
        }

        if (newEvent.getTitle() == null || newEvent.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Event title cannot be empty");
        }
        if (newEvent.getDateTime() == null) {
            throw new IllegalArgumentException("Event date/time cannot be null");
        }
        if (newEvent.getMaxParticipants() <= 0) {
            throw new IllegalArgumentException("Maximum participants must be greater than 0");
        }
        // Validate featuredGame structure (at least ID must be present)
        if (newEvent.getFeaturedGame() == null || newEvent.getFeaturedGame().getId() == 0) {
             throw new IllegalArgumentException("Featured game ID must be provided");
        }
        // Fetch the featured game using the ID from the request
        // Fetch the host account from the authenticated principal
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Account host = accountRepository.findByEmail(username)
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user account not found."));

        int gameId = newEvent.getFeaturedGame().getId();
        Game featuredGameEntity = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Featured game with ID " + gameId + " not found."));

        Event e = new Event(
                newEvent.getTitle(),
                newEvent.getDateTime(),
                newEvent.getLocation(),
                newEvent.getDescription(),
                newEvent.getMaxParticipants(),
                featuredGameEntity, // Use the fetched Game entity
                host // Use the fetched host account
        );

        System.out.println("DEBUG SERVICE: Created event object, saving to repository");
        Event savedEvent = eventRepository.save(e);
        System.out.println("DEBUG SERVICE: Saved event with ID: " + savedEvent.getId());
    return savedEvent;
    } // End of createEvent method

    /**
     * Helper method for @PreAuthorize to check if the authenticated user is the host of the event.
     *
     * @param eventId The ID of the event.
     * @param username The username (email) of the authenticated user.
     * @return true if the user is the host, false otherwise.
     */
    public boolean isHost(UUID eventId, String username) {
        if (username == null) return false;
        try {
            Event event = eventRepository.findEventById(eventId).orElse(null);
            Account user = accountRepository.findByEmail(username).orElse(null);

            if (event == null || user == null || event.getHost() == null) {
                return false; // Cannot determine host or user
            }

            // Compare IDs for accurate check
            return event.getHost().getId() == user.getId();
        } catch (Exception e) {
            // Log error maybe
            System.err.println("Error during isHost check: " + e.getMessage());
            return false; // Deny access on error
        }
    }

    /**
     * Retrieves an event by its unique identifier.
     *
     * @param id The ID of the event to retrieve
     * @return The Event object
     * @throws IllegalArgumentException if no event is found with the given ID
     */
    @Transactional
    public Event getEventById(UUID id) {
        Event event = eventRepository.findEventById(id)
                .orElseThrow(() -> new IllegalArgumentException("Event with id " + id + " does not exist"));

        // Handle date conversion if needed
        if (event.getDateTime() instanceof java.sql.Timestamp) {
            java.sql.Timestamp timestamp = (java.sql.Timestamp) event.getDateTime();
            event.setDateTime(new java.sql.Date(timestamp.getTime()));
        }

        return event;
    }

    /**
     * Retrieves all events in the system.
     *
     * @return List of all Event objects
     */
    @Transactional
    public List<Event> getAllEvents() {
        List<Event> events = eventRepository.findAll();

        // Eagerly initialize lazy-loaded associations needed for DTO conversion
        for (Event event : events) {
            // Accessing getters within the transaction forces initialization
            if (event.getHost() != null) event.getHost().getName(); // Initialize host
            if (event.getFeaturedGame() != null) event.getFeaturedGame().getName(); // Initialize game

            // Also handle date conversion here as done elsewhere
            if (event.getDateTime() instanceof java.sql.Timestamp) { // Check specifically for Timestamp if that's the issue
                java.sql.Timestamp timestamp = (java.sql.Timestamp) event.getDateTime();
                event.setDateTime(new java.sql.Date(timestamp.getTime()));
            } else if (event.getDateTime() instanceof java.util.Date) { // Fallback for general util.Date
                java.util.Date utilDate = (java.util.Date) event.getDateTime();
                event.setDateTime(new java.sql.Date(utilDate.getTime()));
            }
        }

        return events;
    }

    /**
     * Updates an existing event's information.
     *
     * @param id The ID of the event to update
.
     * @param title The new title for the event (optional)
.
     * @param dateTime The new date and time for the event (optional)
.
     * @param location The new location for the event (optional)
.
     * @param description The new description for the event (optional)
.
     * @param maxParticipants The new maximum number of participants (must be greater than 0)
     * @param userEmail The email of the user attempting the update.
     * @return The updated Event object.
     * @throws IllegalArgumentException if the event is not found or if maxParticipants is invalid
.
     * @throws ResponseStatusException if the user attempting the update is not the host (HttpStatus.FORBIDDEN).
     */
    @Transactional
    @PreAuthorize("@eventService.isHost(#id, authentication.principal.username)")
    public Event updateEvent(UUID id, String title, Date dateTime,
                            String location, String description, int maxParticipants) { // Removed userEmail parameter
        try {
            Event event = eventRepository.findEventById(id).orElseThrow(
                    () -> new IllegalArgumentException("Event with id " + id + " does not exist")
            );

            // Authorization is handled by @PreAuthorize

            if (title != null && !title.trim().isEmpty()) {
                event.setTitle(title);
            }
            if (dateTime != null) {
                event.setDateTime(dateTime);
            }
            if (location != null) {
                event.setLocation(location);
            }
            if (description != null) {
                event.setDescription(description);
            }
            // Use default value logic from controller or ensure maxParticipants > 0
            if (maxParticipants > 0) { // Check if a valid value was provided
                 event.setMaxParticipants(maxParticipants);
            } else if (maxParticipants == 0) {
                 // If defaultValue was used in controller (0), don't update unless explicitly needed.
                 // Or, decide if 0 should reset/clear it, or be ignored. Let's ignore 0 for now.
                 System.out.println("DEBUG SERVICE: Ignoring maxParticipants=0 update.");
            } else {
                 // If negative value somehow passed, throw error.
                 throw new IllegalArgumentException("Maximum participants must be positive.");
            }


            System.out.println("DEBUG SERVICE: Saving updated event with ID: " + event.getId());
            return eventRepository.save(event);
        } catch (IllegalArgumentException e) {
             // Re-throw specific exceptions if needed, or let GlobalExceptionHandler handle them
             throw e;
        } catch (org.springframework.security.access.AccessDeniedException e) {
            // Catch potential AccessDeniedException from @PreAuthorize and convert to ForbiddenException
            throw new ForbiddenException("Access denied: You are not the host of this event.");
       }
    }



    /**
     * Deletes an event from the system.
     *
     * @param id The ID of the event to delete
.
     * @param userEmail The email of the user attempting the deletion.
     * @return ResponseEntity with deletion confirmation message
.
     * @throws IllegalArgumentException if no event is found with the given ID
.
     * @throws ResponseStatusException if the user attempting the deletion is not the host (HttpStatus.FORBIDDEN).
     */

    @Transactional
    @PreAuthorize("@eventService.isHost(#id, authentication.principal.username)")
    public ResponseEntity<String> deleteEvent(UUID id) { // Removed userEmail parameter
         try {
            Event eventToDelete = eventRepository.findEventById(id).orElseThrow(
                    () -> new IllegalArgumentException("Event with id " + id + " does not exist")
            );

            // Authorization is handled by @PreAuthorize

            System.out.println("DEBUG SERVICE: Deleting event with ID: " + id);
            eventRepository.delete(eventToDelete);
            return ResponseEntity.ok("Event with id " + id + " has been deleted");
        } catch (IllegalArgumentException e) {
             // Re-throw specific exceptions if needed, or let GlobalExceptionHandler handle them
             throw e;
        } catch (org.springframework.security.access.AccessDeniedException e) {
            // Catch potential AccessDeniedException from @PreAuthorize and convert to ForbiddenException
            throw new ForbiddenException("Access denied: You are not the host of this event.");
       }
    }

    /**
     * Finds events scheduled on a specific date.
     * Handles date type conversion to ensure consistent results.
     *
     * @param date The date to search for
     * @return List of events scheduled on the given date
     */
    @Transactional
    public List<Event> findEventsByDate(java.sql.Date date) {
        List<Event> events = eventRepository.findEventByDateTime(date);

        for (Event event : events) {
            if (event.getDateTime() instanceof java.sql.Timestamp) {
                java.sql.Timestamp timestamp = (java.sql.Timestamp) event.getDateTime();
                event.setDateTime(new java.sql.Date(timestamp.getTime()));
            }
        }

        return events;
    }

    /**
     * Finds events featuring a specific game by the game's ID.
     *
     * @param gameId The ID of the featured game
     * @return List of events featuring the specified game
     */
    @Transactional
    public List<Event> findEventsByGameId(int gameId) {
        List<Event> events = eventRepository.findEventByFeaturedGameId(gameId);

        for (Event event : events) {
            if (event.getDateTime() instanceof java.sql.Timestamp) {
                java.sql.Timestamp timestamp = (java.sql.Timestamp) event.getDateTime();
                event.setDateTime(new java.sql.Date(timestamp.getTime()));
            }
        }

        return events;
    }

    /**
     * Finds events featuring a specific game by the game's name.
     *
     * @param gameName The name of the featured game
     * @return List of events featuring the specified game
     */
    @Transactional
    public List<Event> findEventsByGameName(String gameName) {
        if (gameName == null || gameName.trim().isEmpty()) {
            throw new IllegalArgumentException("Game name cannot be empty");
        }

        List<Event> events = eventRepository.findEventByFeaturedGameName(gameName);

        for (Event event : events) {
            if (event.getDateTime() instanceof java.sql.Timestamp) {
                java.sql.Timestamp timestamp = (java.sql.Timestamp) event.getDateTime();
                event.setDateTime(new java.sql.Date(timestamp.getTime()));
            }
        }

        return events;
    }

    /**
     * Finds events hosted by a specific user by the host's ID.
     *
     * @param hostId The ID of the host
     * @return List of events hosted by the specified user
     */
    @Transactional
    public List<Event> findEventsByHostId(int hostId) {
        List<Event> events = eventRepository.findEventByHostId(hostId);

        for (Event event : events) {
            if (event.getDateTime() instanceof java.sql.Timestamp) {
                java.sql.Timestamp timestamp = (java.sql.Timestamp) event.getDateTime();
                event.setDateTime(new java.sql.Date(timestamp.getTime()));
            }
        }

        return events;
    }

    /**
     * Finds events hosted by a specific user by the host's username.
     *
     * @param hostUsername The username of the host
     * @return List of events hosted by the specified user
     */
    @Transactional
    public List<Event> findEventsByHostEmail(String hostEmail) { // Renamed method and parameter
        if (hostEmail == null || hostEmail.trim().isEmpty()) {
            throw new IllegalArgumentException("Host email cannot be empty");
        }

        List<Event> events = eventRepository.findEventByHostEmail(hostEmail); // Call new repository method

        // Eagerly initialize lazy-loaded associations needed for DTO conversion
        for (Event event : events) {
             // Accessing getters within the transaction forces initialization
             if (event.getHost() != null) event.getHost().getName(); // Initialize host
             if (event.getFeaturedGame() != null) event.getFeaturedGame().getName(); // Initialize game

            if (event.getDateTime() instanceof java.sql.Timestamp) {
                java.sql.Timestamp timestamp = (java.sql.Timestamp) event.getDateTime();
                event.setDateTime(new java.sql.Date(timestamp.getTime()));
            }
        }

        return events;
    }

    /**
     * Finds events where the featured game has a specific minimum number of players.
     * Useful for filtering events based on game characteristics.
     *
     * @param minPlayers The minimum number of players for the featured game
     * @return List of events with games matching the minimum player count
     */
    @Transactional
    public List<Event> findEventsByGameMinPlayers(int minPlayers) {
        if (minPlayers <= 0) {
            throw new IllegalArgumentException("Minimum players must be greater than 0");
        }

        List<Event> events = eventRepository.findByFeaturedGameMinPlayersGreaterThanEqual(minPlayers);

        for (Event event : events) {
            if (event.getDateTime() instanceof java.sql.Timestamp) {
                java.sql.Timestamp timestamp = (java.sql.Timestamp) event.getDateTime();
                event.setDateTime(new java.sql.Date(timestamp.getTime()));
            }
        }
        return events;
    }

    /**
     * Finds events by location, with partial matching supported.
     *
     * @param location The location text to search for
     * @return List of events at locations matching the search text
     */
    @Transactional
    public List<Event> findEventsByLocationContaining(String location) {
        if (location == null || location.trim().isEmpty()) {
            throw new IllegalArgumentException("Location search text cannot be empty");
        }

        List<Event> events = eventRepository.findEventByLocationContaining(location);

        for (Event event : events) {
            if (event.getDateTime() instanceof java.sql.Timestamp) {
                java.sql.Timestamp timestamp = (java.sql.Timestamp) event.getDateTime();
                event.setDateTime(new java.sql.Date(timestamp.getTime()));
            }
        }

        return events;
    }

    /**
     * Finds events by title, with partial matching supported.
     *
     * @param description The event title to search for
     * @return List of events with title matching the search text
     */
    @Transactional
    public List<Event> findEventByTitle(String title){
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Title search text cannot be empty");
        }

        List<Event> events = eventRepository.findEventByTitleContaining(title);

        for (Event event : events) {
            if (event.getDateTime() instanceof java.sql.Timestamp) {
                java.sql.Timestamp timestamp = (java.sql.Timestamp) event.getDateTime();
                event.setDateTime(new java.sql.Date(timestamp.getTime()));
            }
        }

        return events;
    }
}
