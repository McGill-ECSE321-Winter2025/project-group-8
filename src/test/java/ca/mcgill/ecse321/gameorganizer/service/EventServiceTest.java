package ca.mcgill.ecse321.gameorganizer.service;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import ca.mcgill.ecse321.gameorganizer.dto.CreateEventRequest;
import ca.mcgill.ecse321.gameorganizer.models.Account;
import ca.mcgill.ecse321.gameorganizer.models.Event;
import ca.mcgill.ecse321.gameorganizer.models.Game;
import ca.mcgill.ecse321.gameorganizer.repositories.EventRepository;
import ca.mcgill.ecse321.gameorganizer.repositories.GameRepository;
import ca.mcgill.ecse321.gameorganizer.services.EventService;

@ExtendWith(MockitoExtension.class)
public class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private GameRepository gameRepository;

    @InjectMocks
    private EventService eventService;

    // Test constants
    private static final UUID VALID_EVENT_ID = UUID.randomUUID();
    private static final String VALID_TITLE = "Game Night";
    private static final String VALID_LOCATION = "Game Room";
    private static final String VALID_DESCRIPTION = "Fun game night!";
    private static final int VALID_MAX_PARTICIPANTS = 10;

    @Test
    public void testCreateEventSuccess() {
        // Setup
        Date eventDate = new Date(System.currentTimeMillis());
        Game game = new Game("Test Game", 2, 4, "test.jpg", new java.util.Date());
        Account host = new Account("Host", "host@test.com", "password");

        CreateEventRequest request = new CreateEventRequest();
        request.setTitle(VALID_TITLE);
        request.setDateTime(eventDate);
        request.setLocation(VALID_LOCATION);
        request.setDescription(VALID_DESCRIPTION);
        request.setMaxParticipants(VALID_MAX_PARTICIPANTS);
        request.setFeaturedGame(game);
        request.setHost(host);

        Event savedEvent = new Event(VALID_TITLE, eventDate, VALID_LOCATION, VALID_DESCRIPTION, VALID_MAX_PARTICIPANTS, game, host);
        when(eventRepository.save(any(Event.class))).thenReturn(savedEvent);

        // Test
        Event result = eventService.createEvent(request);

        // Verify
        assertNotNull(result);
        assertEquals(VALID_TITLE, result.getTitle());
        assertEquals(VALID_LOCATION, result.getLocation());
        assertEquals(VALID_DESCRIPTION, result.getDescription());
        assertEquals(VALID_MAX_PARTICIPANTS, result.getMaxParticipants());
        verify(eventRepository).save(any(Event.class));
    }

    @Test
    public void testCreateEventWithNullTitle() {
        // Setup
        CreateEventRequest request = new CreateEventRequest();
        request.setTitle(null);
        request.setDateTime(new Date(System.currentTimeMillis()));
        request.setMaxParticipants(VALID_MAX_PARTICIPANTS);
        request.setFeaturedGame(new Game());
        request.setHost(new Account());

        // Test & Verify
        assertThrows(IllegalArgumentException.class, () -> eventService.createEvent(request));
        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    public void testCreateEventWithEmptyTitle() {
        // Setup
        CreateEventRequest request = new CreateEventRequest();
        request.setTitle("  ");
        request.setDateTime(new Date(System.currentTimeMillis()));
        request.setMaxParticipants(VALID_MAX_PARTICIPANTS);
        request.setFeaturedGame(new Game());
        request.setHost(new Account());

        // Test & Verify
        assertThrows(IllegalArgumentException.class, () -> eventService.createEvent(request));
        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    public void testCreateEventWithInvalidMaxParticipants() {
        // Setup
        CreateEventRequest request = new CreateEventRequest();
        request.setTitle(VALID_TITLE);
        request.setDateTime(new Date(System.currentTimeMillis()));
        request.setMaxParticipants(0);
        request.setFeaturedGame(new Game());
        request.setHost(new Account());

        // Test & Verify
        assertThrows(IllegalArgumentException.class, () -> eventService.createEvent(request));
        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    public void testGetEventByIdSuccess() {
        // Setup
        Event event = new Event(VALID_TITLE, new Date(System.currentTimeMillis()), VALID_LOCATION, 
            VALID_DESCRIPTION, VALID_MAX_PARTICIPANTS, new Game(), new Account());
        when(eventRepository.findEventById(VALID_EVENT_ID)).thenReturn(Optional.of(event));

        // Test
        Event result = eventService.getEventById(VALID_EVENT_ID);

        // Verify
        assertNotNull(result);
        assertEquals(VALID_TITLE, result.getTitle());
        verify(eventRepository).findEventById(VALID_EVENT_ID);
    }

    @Test
    public void testGetEventByIdNotFound() {
        // Setup
        when(eventRepository.findEventById(VALID_EVENT_ID)).thenReturn(Optional.empty());

        // Test & Verify
        assertThrows(IllegalArgumentException.class, () -> eventService.getEventById(VALID_EVENT_ID));
        verify(eventRepository).findEventById(VALID_EVENT_ID);
    }

    @Test
    public void testGetAllEvents() {
        // Setup
        List<Event> events = new ArrayList<>();
        events.add(new Event(VALID_TITLE, new Date(System.currentTimeMillis()), VALID_LOCATION, 
            VALID_DESCRIPTION, VALID_MAX_PARTICIPANTS, new Game(), new Account()));
        when(eventRepository.findAll()).thenReturn(events);

        // Test
        List<Event> result = eventService.getAllEvents();

        // Verify
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(VALID_TITLE, result.get(0).getTitle());
        verify(eventRepository).findAll();
    }

    @Test
    public void testUpdateEventSuccess() {
        // Setup
        Event existingEvent = new Event(VALID_TITLE, new Date(System.currentTimeMillis()), VALID_LOCATION, 
            VALID_DESCRIPTION, VALID_MAX_PARTICIPANTS, new Game(), new Account());
        String newTitle = "Updated Event";
        Date newDate = new Date(System.currentTimeMillis());
        String newLocation = "New Location";
        String newDescription = "Updated description";
        int newMaxParticipants = 15;

        when(eventRepository.findEventById(VALID_EVENT_ID)).thenReturn(Optional.of(existingEvent));
        when(eventRepository.save(any(Event.class))).thenReturn(existingEvent);

        // Test
        Event result = eventService.updateEvent(VALID_EVENT_ID, newTitle, newDate, newLocation, 
            newDescription, newMaxParticipants);

        // Verify
        assertNotNull(result);
        assertEquals(newTitle, result.getTitle());
        assertEquals(newLocation, result.getLocation());
        assertEquals(newDescription, result.getDescription());
        assertEquals(newMaxParticipants, result.getMaxParticipants());
        verify(eventRepository).save(any(Event.class));
    }

    @Test
    public void testUpdateEventNotFound() {
        // Setup
        when(eventRepository.findEventById(VALID_EVENT_ID)).thenReturn(Optional.empty());

        // Test & Verify
        assertThrows(IllegalArgumentException.class, () -> 
            eventService.updateEvent(VALID_EVENT_ID, "New Title", new Date(System.currentTimeMillis()), 
                "New Location", "New Description", 15));
        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    public void testDeleteEventSuccess() {
        // Setup
        Event event = new Event(VALID_TITLE, new Date(System.currentTimeMillis()), VALID_LOCATION, 
            VALID_DESCRIPTION, VALID_MAX_PARTICIPANTS, new Game(), new Account());
        when(eventRepository.findEventById(VALID_EVENT_ID)).thenReturn(Optional.of(event));

        // Test
        ResponseEntity<String> response = eventService.deleteEvent(VALID_EVENT_ID);

        // Verify
        assertEquals(200, response.getStatusCodeValue());
        verify(eventRepository).delete(event);
    }

    @Test
    public void testDeleteEventNotFound() {
        // Setup
        when(eventRepository.findEventById(VALID_EVENT_ID)).thenReturn(Optional.empty());

        // Test & Verify
        assertThrows(IllegalArgumentException.class, () -> eventService.deleteEvent(VALID_EVENT_ID));
        verify(eventRepository, never()).delete(any(Event.class));
    }

    @Test
    public void testFindEventsByGameName() {
        // Setup
        String gameName = "Test Game";
        List<Event> events = new ArrayList<>();
        events.add(new Event(VALID_TITLE, new Date(System.currentTimeMillis()), VALID_LOCATION, 
            VALID_DESCRIPTION, VALID_MAX_PARTICIPANTS, new Game(), new Account()));
        when(eventRepository.findEventByFeaturedGameName(gameName)).thenReturn(events);

        // Test
        List<Event> result = eventService.findEventsByGameName(gameName);

        // Verify
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(eventRepository).findEventByFeaturedGameName(gameName);
    }

    @Test
    public void testFindEventsByGameNameEmpty() {
        // Test & Verify
        assertThrows(IllegalArgumentException.class, () -> eventService.findEventsByGameName(""));
        verify(eventRepository, never()).findEventByFeaturedGameName(anyString());
    }

    @Test
    public void testFindEventsByLocationContaining() {
        // Setup
        String locationSearch = "Room";
        List<Event> events = new ArrayList<>();
        events.add(new Event(VALID_TITLE, new Date(System.currentTimeMillis()), VALID_LOCATION, 
            VALID_DESCRIPTION, VALID_MAX_PARTICIPANTS, new Game(), new Account()));
        when(eventRepository.findEventByLocationContaining(locationSearch)).thenReturn(events);

        // Test
        List<Event> result = eventService.findEventsByLocationContaining(locationSearch);

        // Verify
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(eventRepository).findEventByLocationContaining(locationSearch);
    }

    @Test
    public void testFindEventsByLocationContainingEmpty() {
        // Test & Verify
        assertThrows(IllegalArgumentException.class, () -> eventService.findEventsByLocationContaining(""));
        verify(eventRepository, never()).findEventByLocationContaining(anyString());
    }

    @Test
    public void testFindEventsByGameMinPlayers() {
        // Setup
        int minPlayers = 2;
        List<Event> events = new ArrayList<>();
        events.add(new Event(VALID_TITLE, new Date(System.currentTimeMillis()), VALID_LOCATION, 
            VALID_DESCRIPTION, VALID_MAX_PARTICIPANTS, new Game(), new Account()));
        when(eventRepository.findByFeaturedGameMinPlayersGreaterThanEqual(minPlayers)).thenReturn(events);

        // Test
        List<Event> result = eventService.findEventsByGameMinPlayers(minPlayers);

        // Verify
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(eventRepository).findByFeaturedGameMinPlayersGreaterThanEqual(minPlayers);
    }

    @Test
    public void testFindEventsByGameMinPlayersInvalid() {
        // Test & Verify
        assertThrows(IllegalArgumentException.class, () -> eventService.findEventsByGameMinPlayers(0));
        verify(eventRepository, never()).findByFeaturedGameMinPlayersGreaterThanEqual(0);
    }
}
