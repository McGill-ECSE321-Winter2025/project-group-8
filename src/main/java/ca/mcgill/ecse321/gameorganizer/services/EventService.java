package ca.mcgill.ecse321.gameorganizer.services;

import ca.mcgill.ecse321.gameorganizer.models.Event;
import ca.mcgill.ecse321.gameorganizer.repositories.EventRepository;
import ca.mcgill.ecse321.gameorganizer.repositories.GameRepository;
import ca.mcgill.ecse321.gameorganizer.dto.requests.CreateEventRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.Date;



/**
 * Service class that handles business logic for event management operations.
 * Provides methods for creating, retrieving, updating, and deleting gaming events.
 * Ensures business rules and validation for event operations.
 * 
 * @author @Yessine-glitch
 */
@Service
public class EventService {

    private EventRepository eventRepository;

    @Autowired
    private GameRepository gameRepository;

    /**
     * Constructs an EventService with the required repository dependency.
     *
     * @param eventRepository The repository for event data access
     */
    @Autowired
    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    /**
     * Creates a new event in the system after validating required fields.
     *
     * @param newEvent The event object to create
     * @return ResponseEntity with creation confirmation message
     * @throws IllegalArgumentException if required fields are missing or invalid
     */
    @Transactional
    public Event createEvent(CreateEventRequest newEvent) {
        
        if (newEvent.getTitle() == null || newEvent.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Event title cannot be empty");
        }
        if (newEvent.getDateTime() == null) {
            throw new IllegalArgumentException("Event date/time cannot be null");
        }
        if (newEvent.getMaxParticipants() <= 0) {
            throw new IllegalArgumentException("Maximum participants must be greater than 0");
        }
        if (newEvent.getFeaturedGame() == null) {
            throw new IllegalArgumentException("Featured game cannot be null");
        }
        
        Event e = new Event(
            newEvent.getTitle(),
            newEvent.getDateTime(),
            newEvent.getLocation(),
            newEvent.getDescription(),
            newEvent.getMaxParticipants(),
            newEvent.getFeaturedGame()
        );

        return eventRepository.save(e);
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

        for (Event event : events) {
            if (event.getDateTime() instanceof java.util.Date) {
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
     * @param title The new title for the event (optional)
     * @param dateTime The new date and time for the event (optional)
     * @param location The new location for the event (optional)
     * @param description The new description for the event (optional)
     * @param maxParticipants The new maximum number of participants (must be greater than 0)
     * @return ResponseEntity with update confirmation message
     * @throws IllegalArgumentException if the event is not found or if maxParticipants is invalid
     */
    @Transactional
public Event updateEvent(UUID id, String title, Date dateTime, 
        String location, String description, int maxParticipants) {
    Event event = eventRepository.findEventById(id).orElseThrow(
        () -> new IllegalArgumentException("Event with id " + id + " does not exist")
    );

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
    if (maxParticipants > 0) {
        event.setMaxParticipants(maxParticipants);
    }

    return eventRepository.save(event);
}

    /**
     * Deletes an event from the system.
     *
     * @param id The ID of the event to delete
     * @return ResponseEntity with deletion confirmation message
     * @throws IllegalArgumentException if no event is found with the given ID
     */
    @Transactional
    public ResponseEntity<String> deleteEvent(UUID id) {
        Event eventToDelete = eventRepository.findEventById(id).orElseThrow(
            () -> new IllegalArgumentException("Event with id " + id + " does not exist")
        );
        eventRepository.delete(eventToDelete);
        return ResponseEntity.ok("Event with id " + id + " has been deleted");
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
    public List<Event> findEventsByHostName(String hostUsername) {
        if (hostUsername == null || hostUsername.trim().isEmpty()) {
            throw new IllegalArgumentException("Host username cannot be empty");
        }
        
        List<Event> events = eventRepository.findEventByHostName(hostUsername);
        
        for (Event event : events) {
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

        List<Event> events = eventRepository.findEventByLocationContaining(location);
        
        for (Event event : events) {
            if (event.getDateTime() instanceof java.sql.Timestamp) {
                java.sql.Timestamp timestamp = (java.sql.Timestamp) event.getDateTime();
                event.setDateTime(new java.sql.Date(timestamp.getTime()));
            }
        }
        
        return events;
    }
}