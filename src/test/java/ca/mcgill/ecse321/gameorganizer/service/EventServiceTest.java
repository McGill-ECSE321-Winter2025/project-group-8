package ca.mcgill.ecse321.gameorganizer.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Date;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import ca.mcgill.ecse321.gameorganizer.models.Event;
import ca.mcgill.ecse321.gameorganizer.models.Game;
import ca.mcgill.ecse321.gameorganizer.repositories.EventRepository;
import ca.mcgill.ecse321.gameorganizer.repositories.GameRepository;
import ca.mcgill.ecse321.gameorganizer.requests.CreateEventRequest;
import ca.mcgill.ecse321.gameorganizer.services.EventService;

@SpringBootTest
public class EventServiceTest {
    
    @Mock
    private EventRepository eventRepository;
    
    @Mock
    private GameRepository gameRepository;
    
    @InjectMocks
    private EventService eventService;
    
    @Test
    public void testCreateEvent() {
        // Create a mock Game using the constructor from the Game class
        Date currentDate = new Date(System.currentTimeMillis());
        Game mockGame = new Game("Settlers of Catan", 3, 4, "catan.jpg", currentDate);
        mockGame.setId(1); // Set ID manually as it would be set by the database
        mockGame.setCategory("Strategy");
        
        // Setup the event request with the mock game
        CreateEventRequest newEvent = new CreateEventRequest();
        newEvent.setTitle("Catan Tournament");
        newEvent.setDateTime(Date.valueOf(LocalDate.now()));
        newEvent.setLocation("Game Hall");
        newEvent.setDescription("Join our monthly Catan tournament!");
        newEvent.setMaxParticipants(16);
        newEvent.setFeaturedGame(mockGame);
        
        // Create expected event result
        Event event = new Event();
        event.setTitle(newEvent.getTitle());
        event.setDateTime(newEvent.getDateTime());
        event.setLocation(newEvent.getLocation());
        event.setDescription(newEvent.getDescription());
        event.setMaxParticipants(newEvent.getMaxParticipants());
        event.setFeaturedGame(mockGame);
        
        when(eventRepository.save(any(Event.class))).thenReturn(event);
        
        Event createdEvent = eventService.createEvent(newEvent);
        
        assertNotNull(createdEvent);
        assertEquals(newEvent.getTitle(), createdEvent.getTitle());
        assertEquals(newEvent.getDateTime(), createdEvent.getDateTime());
        assertEquals(newEvent.getLocation(), createdEvent.getLocation());
        assertEquals(newEvent.getDescription(), createdEvent.getDescription());
        assertEquals(newEvent.getMaxParticipants(), createdEvent.getMaxParticipants());
        assertEquals(mockGame, createdEvent.getFeaturedGame());
        
        verify(eventRepository, times(1)).save(any(Event.class));
    }
    
    @Test
    public void testCreateEventWithNullGame() {
        CreateEventRequest newEvent = new CreateEventRequest();
        newEvent.setTitle("Test Event");
        newEvent.setDateTime(Date.valueOf(LocalDate.now()));
        newEvent.setLocation("Test Location");
        newEvent.setDescription("Test Description");
        newEvent.setMaxParticipants(10);
        newEvent.setFeaturedGame(null);
        
        // This should throw an IllegalArgumentException
        assertThrows(IllegalArgumentException.class, () -> {
            eventService.createEvent(newEvent);
        });
        
        // Verify that save was never called
        verify(eventRepository, times(0)).save(any(Event.class));
    }
    
    @Test
    public void testCreateEventWithEmptyTitle() {
        // Test with empty title
        CreateEventRequest invalidTitleEvent = new CreateEventRequest();
        invalidTitleEvent.setTitle("");
        invalidTitleEvent.setDateTime(Date.valueOf(LocalDate.now()));
        invalidTitleEvent.setMaxParticipants(10);
        invalidTitleEvent.setFeaturedGame(new Game("Chess", 2, 2, "chess.jpg", new Date(System.currentTimeMillis())));
        
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            eventService.createEvent(invalidTitleEvent);
        });
        
        assertEquals("Event title cannot be empty", exception.getMessage());
        verify(eventRepository, times(0)).save(any(Event.class));
    }
    
    @Test
    public void testCreateEventWithNullDateTime() {
        // Test with null dateTime
        CreateEventRequest nullDateEvent = new CreateEventRequest();
        nullDateEvent.setTitle("Test Event");
        nullDateEvent.setDateTime(null);
        nullDateEvent.setMaxParticipants(10);
        nullDateEvent.setFeaturedGame(new Game("Chess", 2, 2, "chess.jpg", new Date(System.currentTimeMillis())));
        
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            eventService.createEvent(nullDateEvent);
        });
        
        assertEquals("Event date/time cannot be null", exception.getMessage());
        verify(eventRepository, times(0)).save(any(Event.class));
    }
    
    @Test
    public void testCreateEventWithInvalidMaxParticipants() {
        // Test with invalid maxParticipants
        CreateEventRequest invalidParticipantsEvent = new CreateEventRequest();
        invalidParticipantsEvent.setTitle("Test Event");
        invalidParticipantsEvent.setDateTime(Date.valueOf(LocalDate.now()));
        invalidParticipantsEvent.setMaxParticipants(0);
        invalidParticipantsEvent.setFeaturedGame(new Game("Chess", 2, 2, "chess.jpg", new Date(System.currentTimeMillis())));
        
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            eventService.createEvent(invalidParticipantsEvent);
        });
        
        assertEquals("Maximum participants must be greater than 0", exception.getMessage());
        verify(eventRepository, times(0)).save(any(Event.class));
    }

    @Test
    public void testGetEventByValidId() {
        // Arrange
        UUID id = UUID.randomUUID();
        Date currentDate = new Date(System.currentTimeMillis());
        Game game = new Game("Monopoly", 2, 6, "monopoly.jpg", currentDate);

        Event mockEvent = new Event();
        mockEvent.setTitle("Monopoly Night");
        mockEvent.setDateTime(currentDate); 
        mockEvent.setLocation("Board Game Cafe");
        mockEvent.setDescription("Weekly monopoly night");
        mockEvent.setMaxParticipants(12);
        mockEvent.setFeaturedGame(game);
        
        when(eventRepository.findEventById(id)).thenReturn(Optional.of(mockEvent));
        
        // Act
        Event retrievedEvent = eventService.getEventById(id);
        
        // Assert
        assertNotNull(retrievedEvent);
        assertEquals(mockEvent.getTitle(), retrievedEvent.getTitle());
        assertEquals(mockEvent.getDateTime(), retrievedEvent.getDateTime());
        assertEquals(java.sql.Date.class, retrievedEvent.getDateTime().getClass());
    }
    
    @Test
    public void testGetEventByInvalidId() {
        // Arrange
        UUID id = UUID.randomUUID();
        
        // Mock repository to return empty optional for this ID
        when(eventRepository.findEventById(id)).thenReturn(Optional.empty());
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            eventService.getEventById(id);
        });
        
        // Verify the exception message
        assertEquals("Event with id " + id + " does not exist", exception.getMessage());
    }

    @Test
    public void testGetAllEvents() {
        // Arrange - create a list of events with mixed date types
        List<Event> mockEvents = new ArrayList<>();
        
        // Create first event with a SQL Timestamp
        Game chess = new Game("Chess", 2, 2, "chess.jpg", new Date(System.currentTimeMillis()));
        Date chessDate = new Date(System.currentTimeMillis());
        Event chessEvent = new Event();
        chessEvent.setTitle("Chess Tournament");
        chessEvent.setDateTime(chessDate);
        chessEvent.setLocation("Chess Club");
        chessEvent.setDescription("Annual chess tournament");
        chessEvent.setMaxParticipants(16);
        chessEvent.setFeaturedGame(chess);
        mockEvents.add(chessEvent);
        
        // Create second event with a regular Date
        Game monopoly = new Game("Monopoly", 2, 6, "monopoly.jpg", new Date(System.currentTimeMillis()));
        java.sql.Date monopolyDate = new java.sql.Date(System.currentTimeMillis());
        Event monopolyEvent = new Event();
        monopolyEvent.setTitle("Monopoly Night");
        monopolyEvent.setDateTime(monopolyDate);
        monopolyEvent.setLocation("Board Game Cafe");
        monopolyEvent.setDescription("Weekly monopoly night");
        monopolyEvent.setMaxParticipants(12);
        monopolyEvent.setFeaturedGame(monopoly);
        mockEvents.add(monopolyEvent);
        
        // Create third event with a SQL Timestamp
        Game catan = new Game("Settlers of Catan", 3, 4, "catan.jpg", new Date(System.currentTimeMillis()));
        Date catanDate = new Date(System.currentTimeMillis()); // One hour in the future
        Event catanEvent = new Event();
        catanEvent.setTitle("Catan Championship");
        catanEvent.setDateTime(catanDate);
        catanEvent.setLocation("Game Center");
        catanEvent.setDescription("Monthly Catan championship");
        catanEvent.setMaxParticipants(20);
        catanEvent.setFeaturedGame(catan);
        mockEvents.add(catanEvent);
        
        // Mock repository to return our list of events
        when(eventRepository.findAll()).thenReturn(mockEvents);
        
        // Act
        List<Event> retrievedEvents = eventService.getAllEvents();
        
        // Assert
        assertNotNull(retrievedEvents);
        assertEquals(3, retrievedEvents.size());
        
        // Verify all events were returned and timestamps were converted
        for (int i = 0; i < retrievedEvents.size(); i++) {
            Event originalEvent = mockEvents.get(i);
            Event retrievedEvent = retrievedEvents.get(i);
            
            assertEquals(originalEvent.getTitle(), retrievedEvent.getTitle());
            assertEquals(originalEvent.getLocation(), retrievedEvent.getLocation());
            assertEquals(originalEvent.getDescription(), retrievedEvent.getDescription());
            assertEquals(originalEvent.getMaxParticipants(), retrievedEvent.getMaxParticipants());
            assertEquals(originalEvent.getFeaturedGame(), retrievedEvent.getFeaturedGame());
            
            // Verify date type is java.sql.Date for all events
            assertEquals(java.sql.Date.class, retrievedEvent.getDateTime().getClass());
            
        }
    }
    
    @Test
    public void testGetAllEventsEmpty() {
        // Arrange - mock repository to return an empty list
        when(eventRepository.findAll()).thenReturn(new ArrayList<>());
        
        // Act
        List<Event> retrievedEvents = eventService.getAllEvents();
        
        // Assert
        assertNotNull(retrievedEvents);
        assertEquals(0, retrievedEvents.size());
    }

    private UUID eventId;
    private Event existingEvent;
    private Game game;

    @BeforeEach
    public void setUp() {
        // Common setup for all tests
        eventId = UUID.randomUUID();
        game = new Game("Chess", 2, 2, "chess.jpg", new Date(System.currentTimeMillis()));
        
        existingEvent = new Event();
        existingEvent.setTitle("Original Chess Tournament");
        existingEvent.setDateTime(new Date(System.currentTimeMillis()));
        existingEvent.setLocation("Original Location");
        existingEvent.setDescription("Original Description");
        existingEvent.setMaxParticipants(10);
        existingEvent.setFeaturedGame(game);
        
        // Mock repository to return our event for the given ID
        when(eventRepository.findEventById(eventId)).thenReturn(Optional.of(existingEvent));
        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }
    
    @Test
    public void testUpdateAllEventFields() {
        // Arrange
        String newTitle = "Updated Chess Tournament";
        Date newDate = new Date(System.currentTimeMillis() + 86400000); // Tomorrow
        String newLocation = "New Chess Club";
        String newDescription = "Updated annual chess tournament";
        int newMaxParticipants = 20;
        
        // Act
        Event updatedEvent = eventService.updateEvent(
            eventId, 
            newTitle, 
            newDate, 
            newLocation, 
            newDescription, 
            newMaxParticipants
        );
        
        // Assert
        assertNotNull(updatedEvent);
        assertEquals(newTitle, updatedEvent.getTitle());
        assertEquals(newDate, updatedEvent.getDateTime());
        assertEquals(newLocation, updatedEvent.getLocation());
        assertEquals(newDescription, updatedEvent.getDescription());
        assertEquals(newMaxParticipants, updatedEvent.getMaxParticipants());
        assertEquals(game, updatedEvent.getFeaturedGame()); // Game should remain unchanged
        
        // Verify repository interactions
        verify(eventRepository).findEventById(eventId);
        verify(eventRepository).save(existingEvent);
    }
    
    @Test
    public void testUpdateEventWithNullFields() {
        // Arrange - only update some fields, leave others null
        String newTitle = "Partial Update Tournament";
        int newMaxParticipants = 15;
        
        // Original values to verify they don't change
        Date originalDate = (Date) existingEvent.getDateTime();
        String originalLocation = existingEvent.getLocation();
        String originalDescription = existingEvent.getDescription();
        
        // Act - pass null for fields we don't want to update
        Event updatedEvent = eventService.updateEvent(
            eventId, 
            newTitle, 
            null, 
            null, 
            null, 
            newMaxParticipants
        );
        
        // Assert - verify only specified fields are updated
        assertNotNull(updatedEvent);
        assertEquals(newTitle, updatedEvent.getTitle());
        assertEquals(originalDate, updatedEvent.getDateTime());
        assertEquals(originalLocation, updatedEvent.getLocation());
        assertEquals(originalDescription, updatedEvent.getDescription());
        assertEquals(newMaxParticipants, updatedEvent.getMaxParticipants());
        
        // Verify repository interactions
        verify(eventRepository).findEventById(eventId);
        verify(eventRepository).save(existingEvent);
    }
    
    @Test
    public void testUpdateEventWithEmptyTitle() {
        // Arrange - try to update with empty title
        String emptyTitle = "   "; // Just whitespace
        
        // Original values to verify they don't change
        String originalTitle = existingEvent.getTitle();
        
        // Act
        Event updatedEvent = eventService.updateEvent(
            eventId, 
            emptyTitle, 
            null, 
            null, 
            null, 
            0
        );
        
        // Assert - title should not be updated with empty value
        assertNotNull(updatedEvent);
        assertEquals(originalTitle, updatedEvent.getTitle());
        
        // Verify repository interactions
        verify(eventRepository).findEventById(eventId);
        verify(eventRepository).save(existingEvent);
    }
    
    @Test
    public void testUpdateEventWithInvalidMaxParticipants() {
        // Arrange
        int invalidMaxParticipants = 0; // Invalid value
        int originalMaxParticipants = existingEvent.getMaxParticipants();
        
        // Act
        Event updatedEvent = eventService.updateEvent(
            eventId, 
            null, 
            null, 
            null, 
            null, 
            invalidMaxParticipants
        );
        
        // Assert - maxParticipants should not be updated with invalid value
        assertNotNull(updatedEvent);
        assertEquals(originalMaxParticipants, updatedEvent.getMaxParticipants());
        
        // Verify repository interactions
        verify(eventRepository).findEventById(eventId);
        verify(eventRepository).save(existingEvent);
    }
    
    @Test
    public void testUpdateNonExistentEvent() {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();
        when(eventRepository.findEventById(nonExistentId)).thenReturn(Optional.empty());
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            eventService.updateEvent(nonExistentId, "New Title", new Date(System.currentTimeMillis()), "New Location", "New Description", 15);
        });
        
        // Verify exception message
        assertEquals("Event with id " + nonExistentId + " does not exist", exception.getMessage());
        
        // Verify repository interaction - only findById should be called, not save
        verify(eventRepository).findEventById(nonExistentId);
        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    public void testDeleteExistingEvent() {
        // Arrange
        UUID eventId = UUID.randomUUID();
        
        // Create a mock event
        Event eventToDelete = new Event();
        eventToDelete.setTitle("Chess Tournament");
        eventToDelete.setDateTime(new Date(System.currentTimeMillis()));
        eventToDelete.setLocation("Chess Club");
        eventToDelete.setDescription("Event to be deleted");
        eventToDelete.setMaxParticipants(10);
        eventToDelete.setFeaturedGame(new Game("Chess", 2, 2, "chess.jpg", new Date(System.currentTimeMillis())));
        
        // Mock repository behavior
        when(eventRepository.findEventById(eventId)).thenReturn(Optional.of(eventToDelete));
        doNothing().when(eventRepository).delete(eventToDelete);
        
        // Act
        ResponseEntity<String> response = eventService.deleteEvent(eventId);
        
        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Event with id " + eventId + " has been deleted", response.getBody());
        
        // Verify repository methods were called
        verify(eventRepository).findEventById(eventId);
        verify(eventRepository).delete(eventToDelete);
    }
    
    @Test
    public void testDeleteNonExistentEvent() {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();
        
        // Mock repository to return empty for the non-existent ID
        when(eventRepository.findEventById(nonExistentId)).thenReturn(Optional.empty());
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            eventService.deleteEvent(nonExistentId);
        });
        
        // Verify exception message
        assertEquals("Event with id " + nonExistentId + " does not exist", exception.getMessage());
        
        // Verify repository interaction - only findById should be called, not delete
        verify(eventRepository).findEventById(nonExistentId);
        verify(eventRepository, never()).delete(any(Event.class));
    }
}