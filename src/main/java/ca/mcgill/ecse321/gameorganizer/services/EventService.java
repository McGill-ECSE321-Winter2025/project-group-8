package ca.mcgill.ecse321.gameorganizer.services;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;

import ca.mcgill.ecse321.gameorganizer.dto.CreateEventRequest;
import ca.mcgill.ecse321.gameorganizer.models.Account;
import ca.mcgill.ecse321.gameorganizer.models.Event;
import ca.mcgill.ecse321.gameorganizer.models.Game;
import ca.mcgill.ecse321.gameorganizer.models.LendingRecord;
import ca.mcgill.ecse321.gameorganizer.models.GameOwner;
// import ca.mcgill.ecse321.gameorganizer.models.LendingRecord.LendingStatus; // No longer needed directly here due to FQN access
import ca.mcgill.ecse321.gameorganizer.repositories.AccountRepository;
import ca.mcgill.ecse321.gameorganizer.repositories.EventRepository;
import ca.mcgill.ecse321.gameorganizer.repositories.GameRepository;
import ca.mcgill.ecse321.gameorganizer.repositories.LendingRecordRepository;



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

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private LendingRecordRepository lendingRecordRepository;

    /**
     * Constructs an EventService with the required repository dependency.
     *
     * @param eventRepository The repository for event data access
     */
    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    /**
     * Creates a new event in the system after validating required fields.
     *
     * @param newEvent The DTO containing event details (excluding host).
     * @param hostEmail The email of the account hosting the event.
     * @return The created Event object.
     * @throws IllegalArgumentException if required fields are missing or invalid
     */
    @Transactional
    public Event createEvent(CreateEventRequest newEvent, String hostEmail) {
 

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
        // Fetch the host account using the provided email
        Account host = accountRepository.findByEmail(hostEmail)
                .orElseThrow(() -> new IllegalArgumentException("Host account with email " + hostEmail + " not found."));
 
        Event e = new Event(
                newEvent.getTitle(),
                newEvent.getDateTime(),
                newEvent.getLocation(),
                newEvent.getDescription(),
                newEvent.getMaxParticipants(),
                newEvent.getFeaturedGame(),
                host // Use the fetched host account
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
    public Event updateEvent(UUID id, String title, Date dateTime,
                            String location, String description, int maxParticipants, String userEmail) {
        Event event = eventRepository.findEventById(id).orElseThrow(
                () -> new IllegalArgumentException("Event with id " + id + " does not exist")
        );

 
        // Ownership check
        if (event.getHost() == null || !event.getHost().getEmail().equals(userEmail)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the host can update the event.");
        }
 
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
        // Throw an exception if maxParticipants is not valid
        if (maxParticipants <= 0) {
            throw new IllegalArgumentException("Invalid maxParticipants value");
        } else {
            event.setMaxParticipants(maxParticipants);
        }

        return eventRepository.save(event);
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
    public ResponseEntity<String> deleteEvent(UUID id, String userEmail) {
        Event eventToDelete = eventRepository.findEventById(id).orElseThrow(
                () -> new IllegalArgumentException("Event with id " + id + " does not exist")
        );
 
        // Ownership check
        if (eventToDelete.getHost() == null || !eventToDelete.getHost().getEmail().equals(userEmail)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the host can delete the event.");
        }
 
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
     * Retrieves a list of games available for a user to associate with a new event.
     * This includes games owned by the user and games they are currently actively borrowing.
     *
     * @param userEmail The email of the user creating the event.
     * @return A list of unique Game objects available to the user.
     * @throws IllegalArgumentException if the userEmail is invalid or the user is not found.
     */
    public List<Game> getAvailableGamesForEventCreation(String userEmail) {
        if (userEmail == null || userEmail.trim().isEmpty()) {
            throw new IllegalArgumentException("User email cannot be empty.");
        } // Correctly placed brace

        // Corrected Optional handling
        Account user = accountRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User with email " + userEmail + " not found."));

        // Use a Set to automatically handle duplicates
        Set<Game> availableGames = new HashSet<>();

        // 1. Get games owned by the user
        // Check if the user is a GameOwner before fetching owned games
        List<Game> ownedGames = new ArrayList<>(); // Initialize as empty
        if (user instanceof GameOwner) {
            ownedGames = gameRepository.findByOwner((GameOwner) user); // Cast and call findByOwner
        }
        if (ownedGames != null) {
            availableGames.addAll(ownedGames);
        }

        // 2. Get games actively borrowed by the user
        // Fetch all records for the borrower, then filter by status and map to game
        List<LendingRecord> userLendingRecords = lendingRecordRepository.findByRequest_Requester(user); // Use correct method
        if (userLendingRecords != null) {
            List<Game> borrowedGames = userLendingRecords.stream()
                .filter(record -> record.getStatus() == LendingRecord.LendingStatus.ACTIVE) // Filter for ACTIVE status
                    .map(record -> record.getRequest() != null ? record.getRequest().getRequestedGame() : null) // Map to Game via BorrowRequest
                    .filter(java.util.Objects::nonNull) // Filter out null games
                    .collect(Collectors.toList());
            availableGames.addAll(borrowedGames);
        }

        // Convert the Set back to a List for the return type
        return new ArrayList<>(availableGames);
    }
}