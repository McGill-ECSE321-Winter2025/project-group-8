package ca.mcgill.ecse321.gameorganizer.service;

import ca.mcgill.ecse321.gameorganizer.dtos.CreateEventRequest;
import ca.mcgill.ecse321.gameorganizer.models.Account;
import ca.mcgill.ecse321.gameorganizer.models.Event;
import ca.mcgill.ecse321.gameorganizer.models.Game;
import ca.mcgill.ecse321.gameorganizer.repositories.EventRepository;
import ca.mcgill.ecse321.gameorganizer.repositories.GameRepository;
import ca.mcgill.ecse321.gameorganizer.services.EventService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
public class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private GameRepository gameRepository;

    @InjectMocks
    private EventService eventService;

    private Event event;
    private Game game;
    private Account host;
    private Date eventDate;
    private CreateEventRequest eventRequest;

    @BeforeEach
    public void setUp() {
        // Create sample data for tests
        eventDate = new Date(System.currentTimeMillis());
        game = new Game("Chess", 2, 2, "chess.jpg", new java.util.Date());
        game.setId(1);

        host = new Account("Test Host", "host@test.com", "password123");
        host.setId(1);

        event = new Event(
                "Chess Tournament",
                eventDate,
                "Game Center",
                "Monthly chess tournament",
                16,
                game,
                host
        );
        event.setId(1);

        eventRequest = new CreateEventRequest();
        eventRequest.setTitle("Chess Tournament");
        eventRequest.setDateTime(eventDate);
        eventRequest.setLocation("Game Center");
        eventRequest.setDescription("Monthly chess tournament");
        eventRequest.setMaxParticipants(16);
        eventRequest.setFeaturedGame(game);
        eventRequest.setHost(host);
    }

    // Create Event Tests
    @Test
    public void testCreateEventSuccess() {
        // Setup
        when(eventRepository.save(any(Event.class))).thenReturn(event);

        // Call the method
        Event result = eventService.createEvent(eventRequest);

        // Assert
        assertNotNull(result);
        assertEquals("Chess Tournament", result.getTitle());
        assertEquals("Game Center", result.getLocation());
        assertEquals(16, result.getMaxParticipants());
        verify(eventRepository, times(1)).save(any(Event.class));
    }

    @Test
    public void testCreateEventWithEmptyTitle() {
        // Setup
        eventRequest.setTitle("");

        // Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            eventService.createEvent(eventRequest);
        });
        assertEquals("Event title cannot be empty", exception.getMessage());
        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    public void testCreateEventWithNullTitle() {
        // Setup
        eventRequest.setTitle(null);

        // Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            eventService.createEvent(eventRequest);
        });
        assertEquals("Event title cannot be empty", exception.getMessage());
        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    public void testCreateEventWithNullDateTime() {
        // Setup
        eventRequest.setDateTime(null);

        // Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            eventService.createEvent(eventRequest);
        });
        assertEquals("Event date/time cannot be null", exception.getMessage());
        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    public void testCreateEventWithInvalidMaxParticipants() {
        // Setup
        eventRequest.setMaxParticipants(0);

        // Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            eventService.createEvent(eventRequest);
        });
        assertEquals("Maximum participants must be greater than 0", exception.getMessage());
        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    public void testCreateEventWithNullFeaturedGame() {
        // Setup
        eventRequest.setFeaturedGame(null);

        // Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            eventService.createEvent(eventRequest);
        });
        assertEquals("Featured game cannot be null", exception.getMessage());
        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    public void testCreateEventWithNullHost() {
        // Setup
        eventRequest.setHost(null);

        // Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            eventService.createEvent(eventRequest);
        });
        assertEquals("Host cannot be null", exception.getMessage());
        verify(eventRepository, never()).save(any(Event.class));
    }

    // Get Event By ID Tests
    @Test
    public void testGetEventByIdSuccess() {
        // Setup
        when(eventRepository.findEventById(1)).thenReturn(Optional.of(event));

        // Call the method
        Event result = eventService.getEventById(1);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("Chess Tournament", result.getTitle());
    }

    @Test
    public void testGetEventByIdNotFound() {
        // Setup
        when(eventRepository.findEventById(99)).thenReturn(Optional.empty());

        // Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            eventService.getEventById(99);
        });
        assertEquals("Event with id 99 does not exist", exception.getMessage());
    }

    // Get All Events Test
    @Test
    public void testGetAllEventsSuccess() {
        // Setup
        List<Event> events = List.of(event);
        when(eventRepository.findAll()).thenReturn(events);

        // Call the method
        List<Event> results = eventService.getAllEvents();

        // Assert
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("Chess Tournament", results.get(0).getTitle());
    }

    // Update Event Tests
    @Test
    public void testUpdateEventSuccess() {
        // Setup
        when(eventRepository.findEventById(1)).thenReturn(Optional.of(event));

        Event updatedEvent = new Event(
                "Updated Chess Tournament",
                eventDate,
                "New Location",
                "Updated description",
                24,
                game,
                host
        );
        updatedEvent.setId(1);

        when(eventRepository.save(any(Event.class))).thenReturn(updatedEvent);

        // Call the method
        Event result = eventService.updateEvent(
                1,
                "Updated Chess Tournament",
                eventDate,
                "New Location",
                "Updated description",
                24
        );

        // Assert
        assertNotNull(result);
        assertEquals("Updated Chess Tournament", result.getTitle());
        assertEquals("New Location", result.getLocation());
        assertEquals("Updated description", result.getDescription());
        assertEquals(24, result.getMaxParticipants());
        verify(eventRepository, times(1)).save(any(Event.class));
    }

    @Test
    public void testUpdateEventNotFound() {
        // Setup
        when(eventRepository.findEventById(99)).thenReturn(Optional.empty());

        // Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            eventService.updateEvent(
                    99,
                    "Updated Chess Tournament",
                    eventDate,
                    "New Location",
                    "Updated description",
                    24
            );
        });
        assertEquals("Event with id 99 does not exist", exception.getMessage());
        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    public void testUpdateEventWithNullValues() {
        // Setup
        when(eventRepository.findEventById(1)).thenReturn(Optional.of(event));
        when(eventRepository.save(any(Event.class))).thenReturn(event);

        // Call the method with null values
        Event result = eventService.updateEvent(
                1,
                null,
                null,
                null,
                null,
                0
        );

        // Assert that original values are preserved
        assertNotNull(result);
        assertEquals("Chess Tournament", result.getTitle());
        assertEquals("Game Center", result.getLocation());
        assertEquals("Monthly chess tournament", result.getDescription());
        assertEquals(16, result.getMaxParticipants());
        verify(eventRepository, times(1)).save(any(Event.class));
    }

    // Delete Event Tests
    @Test
    public void testDeleteEventSuccess() {
        // Setup
        when(eventRepository.findEventById(1)).thenReturn(Optional.of(event));
        doNothing().when(eventRepository).delete(event);

        // Call the method
        ResponseEntity<String> response = eventService.deleteEvent(1);

        // Assert
        assertEquals("Event with id 1 has been deleted", response.getBody());
        verify(eventRepository, times(1)).delete(event);
    }

    @Test
    public void testDeleteEventNotFound() {
        // Setup
        when(eventRepository.findEventById(99)).thenReturn(Optional.empty());

        // Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            eventService.deleteEvent(99);
        });
        assertEquals("Event with id 99 does not exist", exception.getMessage());
        verify(eventRepository, never()).delete(any(Event.class));
    }

    // Find Events By Date Tests
    @Test
    public void testFindEventsByDateSuccess() {
        // Setup
        List<Event> events = List.of(event);
        java.sql.Date sqlDate = new java.sql.Date(eventDate.getTime());
        when(eventRepository.findEventByDateTime(sqlDate)).thenReturn(events);

        // Call the method
        List<Event> results = eventService.findEventsByDate(sqlDate);

        // Assert
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("Chess Tournament", results.get(0).getTitle());
    }

    // Find Events By Game ID Tests
    @Test
    public void testFindEventsByGameIdSuccess() {
        // Setup
        List<Event> events = List.of(event);
        when(eventRepository.findEventByFeaturedGameId(1)).thenReturn(events);

        // Call the method
        List<Event> results = eventService.findEventsByGameId(1);

        // Assert
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("Chess Tournament", results.get(0).getTitle());
        assertEquals(1, results.get(0).getFeaturedGame().getId());
    }

    // Find Events By Game Name Tests
    @Test
    public void testFindEventsByGameNameSuccess() {
        // Setup
        List<Event> events = List.of(event);
        when(eventRepository.findEventByFeaturedGameName("Chess")).thenReturn(events);

        // Call the method
        List<Event> results = eventService.findEventsByGameName("Chess");

        // Assert
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("Chess Tournament", results.get(0).getTitle());
        assertEquals("Chess", results.get(0).getFeaturedGame().getName());
    }

    @Test
    public void testFindEventsByGameNameEmpty() {
        // Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            eventService.findEventsByGameName("");
        });
        assertEquals("Game name cannot be empty", exception.getMessage());
    }

    @Test
    public void testFindEventsByGameNameNull() {
        // Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            eventService.findEventsByGameName(null);
        });
        assertEquals("Game name cannot be empty", exception.getMessage());
    }

    // Find Events By Host ID Tests
    @Test
    public void testFindEventsByHostIdSuccess() {
        // Setup
        List<Event> events = List.of(event);
        when(eventRepository.findEventByHostId(1)).thenReturn(events);

        // Call the method
        List<Event> results = eventService.findEventsByHostId(1);

        // Assert
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("Chess Tournament", results.get(0).getTitle());
        assertEquals(1, results.get(0).getHost().getId());
    }

    // Find Events By Host Name Tests
    @Test
    public void testFindEventsByHostNameSuccess() {
        // Setup
        List<Event> events = List.of(event);
        when(eventRepository.findEventByHostName("Test Host")).thenReturn(events);

        // Call the method
        List<Event> results = eventService.findEventsByHostName("Test Host");

        // Assert
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("Chess Tournament", results.get(0).getTitle());
        assertEquals("Test Host", results.get(0).getHost().getName());
    }

    @Test
    public void testFindEventsByHostNameEmpty() {
        // Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            eventService.findEventsByHostName("");
        });
        assertEquals("Host username cannot be empty", exception.getMessage());
    }

    @Test
    public void testFindEventsByHostNameNull() {
        // Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            eventService.findEventsByHostName(null);
        });
        assertEquals("Host username cannot be empty", exception.getMessage());
    }

    // Find Events By Game Min Players Tests
    @Test
    public void testFindEventsByGameMinPlayersSuccess() {
        // Setup
        List<Event> events = List.of(event);
        when(eventRepository.findByFeaturedGameMinPlayersGreaterThanEqual(2)).thenReturn(events);

        // Call the method
        List<Event> results = eventService.findEventsByGameMinPlayers(2);

        // Assert
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("Chess Tournament", results.get(0).getTitle());
    }

    @Test
    public void testFindEventsByGameMinPlayersInvalid() {
        // Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            eventService.findEventsByGameMinPlayers(0);
        });
        assertEquals("Minimum players must be greater than 0", exception.getMessage());
    }

    // Find Events By Location Containing Tests
    @Test
    public void testFindEventsByLocationContainingSuccess() {
        // Setup
        List<Event> events = List.of(event);
        when(eventRepository.findEventByLocationContaining("Game")).thenReturn(events);

        // Call the method
        List<Event> results = eventService.findEventsByLocationContaining("Game");

        // Assert
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("Chess Tournament", results.get(0).getTitle());
        assertTrue(results.get(0).getLocation().contains("Game"));
    }

    @Test
    public void testFindEventsByLocationContainingEmpty() {
        // Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            eventService.findEventsByLocationContaining("");
        });
        assertEquals("Location search text cannot be empty", exception.getMessage());
    }

    @Test
    public void testFindEventsByLocationContainingNull() {
        // Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            eventService.findEventsByLocationContaining(null);
        });
        assertEquals("Location search text cannot be empty", exception.getMessage());
    }
}