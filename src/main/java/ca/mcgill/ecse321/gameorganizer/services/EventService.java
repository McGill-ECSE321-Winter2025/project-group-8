package ca.mcgill.ecse321.gameorganizer.services;

import ca.mcgill.ecse321.gameorganizer.models.Event;
import ca.mcgill.ecse321.gameorganizer.repositories.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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
    public ResponseEntity<String> createEvent(Event newEvent) {
        
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

        eventRepository.save(newEvent);
        return ResponseEntity.ok("Event created successfully");
    }

    /**
     * Retrieves an event by its unique identifier.
     *
     * @param id The ID of the event to retrieve
     * @return The Event object
     * @throws IllegalArgumentException if no event is found with the given ID
     */
    @Transactional
    public Event getEventById(int id) {
        return eventRepository.findEventById(id).orElseThrow(
            () -> new IllegalArgumentException("Event with id " + id + " does not exist")
        );
    }

    /**
     * Retrieves all events in the system.
     *
     * @return List of all Event objects
     */
    @Transactional
    public List<Event> getAllEvents() {
        return eventRepository.findAll();
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
    public ResponseEntity<String> updateEvent(int id, String title, Date dateTime, 
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

        eventRepository.save(event);
        return ResponseEntity.ok("Event updated successfully");
    }

    /**
     * Deletes an event from the system.
     *
     * @param id The ID of the event to delete
     * @return ResponseEntity with deletion confirmation message
     * @throws IllegalArgumentException if no event is found with the given ID
     */
    @Transactional
    public ResponseEntity<String> deleteEvent(int id) {
        Event eventToDelete = eventRepository.findEventById(id).orElseThrow(
            () -> new IllegalArgumentException("Event with id " + id + " does not exist")
        );
        eventRepository.delete(eventToDelete);
        return ResponseEntity.ok("Event with id " + id + " has been deleted");
    }
}